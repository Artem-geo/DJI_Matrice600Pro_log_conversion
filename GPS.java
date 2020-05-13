package djilog2text;

import java.text.SimpleDateFormat;
import java.util.*;

public class GPS {
	
	public ArrayList<String> datetime;	
	public ArrayList<Date> date;
	public ArrayList<Long> epoch;
	public ArrayList<Double> lat;
	public ArrayList<Double> lon;
	public ArrayList<Double> heightMSL;
			
	public GPS() {
		this.datetime = new ArrayList<String>();
		this.date = new ArrayList<Date>();
		this.epoch = new ArrayList<Long>();
		this.lat = new ArrayList<Double>();
		this.lon = new ArrayList<Double>();
		this.heightMSL = new ArrayList<Double>();
	}
	
	// populate GPS class by data from input file
	public static void addDataToGPS(GPS gps, int firstColumn, String[] line) {	
		gps.datetime.add(line[firstColumn+2]);
		gps.lat.add(Double.parseDouble(line[firstColumn+1]));
		gps.lon.add(Double.parseDouble(line[firstColumn]));
		gps.heightMSL.add(Double.parseDouble(line[firstColumn+3]));		
	}
	
	// convert and put data into GPS class
	public static void convertTime(GPS gps) throws Exception {
		
		Set<String> uniqueDatetime = new TreeSet<String>(gps.datetime);
		String[] datetimeSeparate = new String[2];
		
		// create date-time object for input
		String pattern = "yyyy-MM-dd HH:mm:ss.SSS";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, new Locale("en", "GB"));
		
		for (String point: uniqueDatetime) {
			int first = gps.datetime.indexOf(point);
			int second = gps.datetime.lastIndexOf(point);
			int pointsAmount = second - first + 1;
			
			double secondsDivision = 1000 / pointsAmount;
			
			datetimeSeparate = point.split("[TZ]");
			
			String ms;
			for (int i=0; i<pointsAmount; i++) {
				ms = ((int)(secondsDivision * i)) + "";
				switch(ms.length()) {
				case 1:
					ms = "00" + ms;
					break;
				case 2:
					ms = "0" + ms;
					break;
				case 3:
					break;
				}
				
				Date d = simpleDateFormat.parse(datetimeSeparate[0] + " " + datetimeSeparate[1] + "." + ms);

				gps.date.add(d);
				gps.epoch.add(d.getTime());
			}

		}
	}
}
