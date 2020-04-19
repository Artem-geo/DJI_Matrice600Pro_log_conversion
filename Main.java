package gps;

import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

public class Main {
	public static void main(String[] args) throws Exception {
		
		// после отладки заменить путь в filereader на args[0]
		//////////////////////////////////////////////////////////////////////////////
		FileReader fr = new FileReader("/home/artem/Documents/TestFiles/flight2_cut.csv");
		FileWriter fw = new FileWriter("/home/artem/Documents/TestFiles/flight2_result.dat");
		/////////////////////////////////////////////////////////////////////////////
		
		///////////////////////////////////////////////////////////////////////////////
		FileWriter gpsOneFile = new FileWriter("/home/artem/Documents/TestFiles/test/gpsOne.dat");
		FileWriter gpsTwoFile = new FileWriter("/home/artem/Documents/TestFiles/test/gpsTwo.dat");
		FileWriter gpsThreeFile = new FileWriter("/home/artem/Documents/TestFiles/test/gpsThree.dat");
		FileWriter gpsResultCompare = new FileWriter("/home/artem/Documents/TestFiles/test/gpsCompare.dat");
		////////////////////////////////////////////////////////////////////////////////
		
		String pattern = "yyyy-MM-dd HH:mm:ss.SSS";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, new Locale("en", "GB"));
        
		Scanner scanner = new Scanner(fr);	
		
		// создаём объекты для трёх gps модулей
		GPS gpsOne = new GPS();
		GPS gpsTwo = new GPS();
		GPS gpsThree = new GPS();
		
		
		// записываем данные в классы GPS: datetime, lat, lon, height(MSL)
		String[] line = new String[12];
		while (scanner.hasNextLine()) {
			line = scanner.nextLine().split(",");
			
			GPS.addDataToGPS(gpsOne, 0, line);
			GPS.addDataToGPS(gpsTwo, 4, line);
			GPS.addDataToGPS(gpsThree, 8, line);
		}
		
		
		// записываем данные в классы GPS: Epoch, Date, Time
		GPS.convertTime(gpsOne);
		GPS.convertTime(gpsTwo);
		GPS.convertTime(gpsThree);
		
		
		
		//создаём и наполняем класс GPS, содержащий средние значения
		GPS gpsResult = new GPS();
		
		Date initGPS = simpleDateFormat.parse("1980-01-06 00:00:00.00");
		
		fw.write("GPST DATE_UTC TIME_UTC LAT LON HEIGHT_MSL\n");
		
		for (int i=20; i<gpsOne.date.size(); i++) {
			
			long epochTimeOne = gpsOne.epoch.get(i);
			
			int indexGpsTwo = getClosestIndex(gpsTwo.epoch, epochTimeOne);
			int indexGpsThree = getClosestIndex(gpsThree.epoch, epochTimeOne);
			
			// среднее время Epoch
			long epochResultmean = (gpsOne.epoch.get(i)+gpsTwo.epoch.get(indexGpsTwo)+gpsThree.epoch.get(indexGpsThree))/3;
			gpsResult.epoch.add(epochResultmean);
			double gpsResultGPST = (double) (epochResultmean - initGPS.getTime())/1000 + 18;
			fw.write(String.format(Locale.ROOT,"%.3f ", gpsResultGPST));
			
			
			// средняя дата и время
			long epochTimeTwo = gpsTwo.date.get(indexGpsTwo).getTime();
			long epochTimeThree = gpsThree.date.get(indexGpsThree).getTime();
			long gpsResultEpochMean = (epochTimeOne + epochTimeTwo + epochTimeThree) / 3;
			Date gpsResultDate = new Date(gpsResultEpochMean);
			fw.write(simpleDateFormat.format(gpsResultDate)+" ");
			
			
			
			// средняя широта, долгота и высота
			Double gpsResultLatTmean = (gpsOne.lat.get(i)+gpsTwo.lat.get(indexGpsTwo)+gpsThree.lat.get(indexGpsThree))/3;
			gpsResult.lat.add(gpsResultLatTmean);
			Double gpsResultLonTmean = (gpsOne.lon.get(i)+gpsTwo.lon.get(indexGpsTwo)+gpsThree.lon.get(indexGpsThree))/3;
			gpsResult.lon.add(gpsResultLonTmean);
			Double gpsResultHeightMSLmean = (gpsOne.heightMSL.get(i)+gpsTwo.heightMSL.get(indexGpsTwo)+gpsThree.heightMSL.get(indexGpsThree))/3;
			gpsResult.heightMSL.add(gpsResultHeightMSLmean);
			fw.write(String.format(Locale.ROOT, "%.7f %.7f %.3f\n", gpsResultLatTmean, gpsResultLonTmean, gpsResultHeightMSLmean));	
			
			
			////////////////////////////////////
			gpsResultCompare.write(String.format("%d %d %d\n", gpsOne.epoch.get(i), gpsTwo.epoch.get(indexGpsTwo), gpsThree.epoch.get(indexGpsThree)));
			gpsResultCompare.write(simpleDateFormat.format(epochTimeOne)+" "+simpleDateFormat.format(epochTimeTwo)+" "+
					simpleDateFormat.format(epochTimeThree)+" "+simpleDateFormat.format(gpsResultDate)+"\n");
			gpsResultCompare.write(String.format("%.7f %.7f %.7f\n",gpsOne.lat.get(i),gpsTwo.lat.get(indexGpsTwo),gpsThree.lat.get(indexGpsThree)));
			gpsResultCompare.write(String.format("%.7f %.7f %.7f\n",gpsOne.lon.get(i),gpsTwo.lon.get(indexGpsTwo),gpsThree.lon.get(indexGpsThree)));
			gpsResultCompare.write(String.format("%.3f %.3f %.3f\n\n\n",gpsOne.heightMSL.get(i),gpsTwo.heightMSL.get(indexGpsTwo),gpsThree.heightMSL.get(indexGpsThree)));
			/////////////////////////////////////
		}
		
		
		
		
		/////////////////////////////////////////////////////////
		for (int i=0; i<gpsOne.date.size(); i++) {
			gpsOneFile.write(String.format("%d %s %.7f %.7f %.3f\n", gpsOne.epoch.get(i), simpleDateFormat.format(gpsOne.date.get(i)),
											gpsOne.lat.get(i), gpsOne.lon.get(i), gpsOne.heightMSL.get(i)));
			gpsTwoFile.write(String.format("%d %s %.7f %.7f %.3f\n", gpsTwo.epoch.get(i), simpleDateFormat.format(gpsTwo.date.get(i)),
					gpsTwo.lat.get(i), gpsTwo.lon.get(i), gpsTwo.heightMSL.get(i)));
			gpsThreeFile.write(String.format("%d %s %.7f %.7f %.3f\n", gpsThree.epoch.get(i), simpleDateFormat.format(gpsThree.date.get(i)),
					gpsThree.lat.get(i), gpsThree.lon.get(i), gpsThree.heightMSL.get(i)));
		}
		gpsOneFile.close();
		gpsTwoFile.close();
		gpsThreeFile.close();
		gpsResultCompare.close();
		////////////////////////////////////////////////////////
		
		fr.close();
		scanner.close();
		fw.close();
	}	

	public static int getClosestIndex(final ArrayList<Long> epoch, long value) {
			class Closest {
		    	long dif;
		    	int index = -1;
		    };
		    Closest closest = new Closest();
		    for (int i = 0; i < epoch.size(); i++) {
		        final long dif = Math.abs(value - epoch.get(i));
		        if (closest.dif == 0 || dif < closest.dif) {
		            closest.index = i;
		            closest.dif = dif;
		        }
		        
		        if(i<epoch.size()-1 && dif < Math.abs(value - epoch.get(i+1))) break; //закончить цикл, если следующее значение больше
		    }
		    return closest.index;
		}
	
}








