package moos.ssds.services.data.graphing;

import java.util.Date;

import javax.ejb.Remote;

import moos.ssds.metadata.Device;

@Remote
public interface GeospatialGraphingAccess {

	/**
	 */
	public abstract String getGpsChart(Device device, Date startDate,
			Date endDate, boolean drawWatchCircle,
			double watchCircleDiameterInKm, boolean scaleChartToFitData,
			boolean plotAnchorLocation, double anchorLatitude,
			double anchorLongitude, int chartXSize, int chartYSize,
			String chartTitle);

}