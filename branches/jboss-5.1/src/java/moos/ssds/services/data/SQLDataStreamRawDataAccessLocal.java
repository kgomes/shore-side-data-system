package moos.ssds.services.data;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;
import java.util.TreeMap;

import javax.ejb.Local;
import javax.ejb.Remote;

@Local
public interface SQLDataStreamRawDataAccessLocal {

	/**
	 * These are the constants that define the types of sorting and filtering
	 * can be done by this service
	 */
	public static final String BY_SEQUENCE_NUMBER = "sequenceNumber";
	public static final String BY_TIMESTAMP = "timestamp";
	/**
	 * Some constants to define what properties are available
	 */
	public static final String NUMBER_OF_RECORDS = "numRecords";
	public static final String DATE_OF_LAST_RECORD = "lastRecordDate";
	public static final String AVERAGE_SAMPLE_INTERVAL_IN_MILLIS = "averageSampleIntervalInMillis";
	public static final String TIME_ONLY_GAP = "timeGap";
	public static final String SEQ_ONLY_GAP = "seqGap";
	public static final String TIME_SEQ_GAP = "timeSeqGap";
	public static final String SERVICE_CALCULATED = "serviceCalculated";
	public static final String USER_SPECIFIED = "userSpecified";

	/**
	 * @ejb.interface-method view-type="both"
	 * @return
	 * @throws SQLException
	 */
	public abstract Collection getDataProducingDeviceIDs() throws SQLException;

	/**
	 * @ejb.interface-method view-type="both"
	 * @return
	 * @throws SQLException
	 */
	public abstract TreeMap getParentChildDataProducerTrees()
			throws SQLException;

	/**
	 * 
	 * @ejb.interface-method view-type="both"
	 * 
	 * @param deviceID
	 * @param recordType
	 * @param checkForGaps
	 * @param typeOfGap
	 * @param marginMillis
	 * @param gapSpec
	 * @param numberOfRecords
	 * @param intervalCalcStartWindow
	 * @param intervalCalcEndWindow
	 * @param gapInMillis
	 * @return
	 * @throws SQLException
	 */
	public abstract Properties getDataStreamProperties(Long deviceID,
			Long recordType, Boolean checkForGaps, Date startGapCheckWindow,
			Date endGapCheckWindow, String typeOfGap, Long marginInMillis,
			String gapSpec, Long numberOfRecords, Date intervalCalcStartWindow,
			Date intervalCalcEndWindow, Long gapInMillis) throws SQLException;

	/**
	 * Note: This method returns a TreeMap with either sequence numbers or
	 * timestamps as the key and a <b><code>Collection</code></b> of
	 * SSDSDevicePackets (or data buffers depending on the input parameters) as
	 * the corresponding value
	 * 
	 * @ejb.interface-method view-type="both"
	 * @param deviceID
	 * @param startParentID
	 * @param endParentID
	 * @param startPacketType
	 * @param endPacketType
	 * @param startPacketSubType
	 * @param endPacketSubType
	 * @param startDataDescriptionID
	 * @param endDataDescriptionID
	 * @param startDataDescriptionVersion
	 * @param endDataDescriptionVersion
	 * @param startTimestampSeconds
	 * @param endTimestampSeconds
	 * @param startTimestampNanoseconds
	 * @param endTimestampNanoseconds
	 * @param startSequenceNumber
	 * @param endSequenceNumber
	 * @param startLatitude
	 * @param endLatitude
	 * @param startLongitude
	 * @param endLongitude
	 * @param startDepth
	 * @param endDepth
	 * @param orderBy
	 * @param returnAsSSDSDevicePackets
	 * @return
	 */
	public abstract TreeMap getSortedRawData(Long deviceID, Long startParentID,
			Long endParentID, Integer startPacketType, Integer endPacketType,
			Long startPacketSubType, Long endPacketSubType,
			Long startDataDescriptionID, Long endDataDescriptionID,
			Long startDataDescriptionVersion, Long endDataDescriptionVersion,
			Long startTimestampSeconds, Long endTimestampSeconds,
			Long startTimestampNanoseconds, Long endTimestampNanoseconds,
			Long startSequenceNumber, Long endSequenceNumber,
			Long lastNumberOfPackets, Double startLatitude, Double endLatitude,
			Double startLongitude, Double endLongitude, Float startDepth,
			Float endDepth, String orderBy, boolean returnAsSSDSDevicePackets)
			throws SQLException;

}