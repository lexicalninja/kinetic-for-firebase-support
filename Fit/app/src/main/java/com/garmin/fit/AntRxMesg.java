////////////////////////////////////////////////////////////////////////////////
// The following FIT Protocol software provided may be used with FIT protocol
// devices only and remains the copyrighted property of Dynastream Innovations Inc.
// The software is being provided on an "as-is" basis and as an accommodation,
// and therefore all warranties, representations, or guarantees of any kind
// (whether express, implied or statutory) including, without limitation,
// warranties of merchantability, non-infringement, or fitness for a particular
// purpose, are specifically disclaimed.
//
// Copyright 2015 Dynastream Innovations Inc.
////////////////////////////////////////////////////////////////////////////////
// ****WARNING****  This file is auto-generated!  Do NOT edit this file.
// Profile Version = 16.30Release
// Tag = development-akw-16.30.00-0
////////////////////////////////////////////////////////////////////////////////


package com.garmin.fit;


public class AntRxMesg extends Mesg {

   protected static final	Mesg antRxMesg;
   static {int field_index = 0;         
      // ant_rx   
      antRxMesg = new Mesg("ant_rx", MesgNum.ANT_RX);
      antRxMesg.addField(new Field("timestamp", 253, 134, 1, 0, "s", false));
      field_index++;
      antRxMesg.addField(new Field("fractional_timestamp", 0, 132, 32768, 0, "s", false));
      field_index++;
      antRxMesg.addField(new Field("mesg_id", 1, 13, 1, 0, "", false));
      field_index++;
      antRxMesg.addField(new Field("mesg_data", 2, 13, 1, 0, "", false)); 
      antRxMesg.fields.get(field_index).components.add(new FieldComponent(3, false, 8, 1, 0)); // channel_number 
      antRxMesg.fields.get(field_index).components.add(new FieldComponent(4, false, 8, 1, 0)); // data 
      antRxMesg.fields.get(field_index).components.add(new FieldComponent(4, false, 8, 1, 0)); // data 
      antRxMesg.fields.get(field_index).components.add(new FieldComponent(4, false, 8, 1, 0)); // data 
      antRxMesg.fields.get(field_index).components.add(new FieldComponent(4, false, 8, 1, 0)); // data 
      antRxMesg.fields.get(field_index).components.add(new FieldComponent(4, false, 8, 1, 0)); // data 
      antRxMesg.fields.get(field_index).components.add(new FieldComponent(4, false, 8, 1, 0)); // data 
      antRxMesg.fields.get(field_index).components.add(new FieldComponent(4, false, 8, 1, 0)); // data 
      antRxMesg.fields.get(field_index).components.add(new FieldComponent(4, false, 8, 1, 0)); // data
      field_index++;
      antRxMesg.addField(new Field("channel_number", 3, 2, 1, 0, "", false));
      field_index++;
      antRxMesg.addField(new Field("data", 4, 13, 1, 0, "", false));
      field_index++;
   }

   public AntRxMesg() {
      super(Factory.createMesg(MesgNum.ANT_RX));
   }

   public AntRxMesg(final Mesg mesg) {
      super(mesg);
   }


   /**
    * Get timestamp field
    * Units: s
    *
    * @return timestamp
    */
   public DateTime getTimestamp() {
      return timestampToDateTime(getFieldLongValue(253, 0, Fit.SUBFIELD_INDEX_MAIN_FIELD));
   }

   /**
    * Set timestamp field
    * Units: s
    *
    * @param timestamp
    */
   public void setTimestamp(DateTime timestamp) {
      setFieldValue(253, 0, timestamp.getTimestamp(), Fit.SUBFIELD_INDEX_MAIN_FIELD);
   }

   /**
    * Get fractional_timestamp field
    * Units: s
    *
    * @return fractional_timestamp
    */
   public Float getFractionalTimestamp() {
      return getFieldFloatValue(0, 0, Fit.SUBFIELD_INDEX_MAIN_FIELD);
   }

   /**
    * Set fractional_timestamp field
    * Units: s
    *
    * @param fractionalTimestamp
    */
   public void setFractionalTimestamp(Float fractionalTimestamp) {
      setFieldValue(0, 0, fractionalTimestamp, Fit.SUBFIELD_INDEX_MAIN_FIELD);
   }

   /**
    * Get mesg_id field
    *
    * @return mesg_id
    */
   public Byte getMesgId() {
      return getFieldByteValue(1, 0, Fit.SUBFIELD_INDEX_MAIN_FIELD);
   }

   /**
    * Set mesg_id field
    *
    * @param mesgId
    */
   public void setMesgId(Byte mesgId) {
      setFieldValue(1, 0, mesgId, Fit.SUBFIELD_INDEX_MAIN_FIELD);
   }

   /**
    * @return number of mesg_data
    */
   public int getNumMesgData() {
      return getNumFieldValues(2, Fit.SUBFIELD_INDEX_MAIN_FIELD);
   }

   /**
    * Get mesg_data field
    *
    * @param index of mesg_data
    * @return mesg_data
    */
   public Byte getMesgData(int index) {
      return getFieldByteValue(2, index, Fit.SUBFIELD_INDEX_MAIN_FIELD);
   }

   /**
    * Set mesg_data field
    *
    * @param index of mesg_data
    * @param mesgData
    */
   public void setMesgData(int index, Byte mesgData) {
      setFieldValue(2, index, mesgData, Fit.SUBFIELD_INDEX_MAIN_FIELD);
   }

   /**
    * Get channel_number field
    *
    * @return channel_number
    */
   public Short getChannelNumber() {
      return getFieldShortValue(3, 0, Fit.SUBFIELD_INDEX_MAIN_FIELD);
   }

   /**
    * Set channel_number field
    *
    * @param channelNumber
    */
   public void setChannelNumber(Short channelNumber) {
      setFieldValue(3, 0, channelNumber, Fit.SUBFIELD_INDEX_MAIN_FIELD);
   }

   /**
    * @return number of data
    */
   public int getNumData() {
      return getNumFieldValues(4, Fit.SUBFIELD_INDEX_MAIN_FIELD);
   }

   /**
    * Get data field
    *
    * @param index of data
    * @return data
    */
   public Byte getData(int index) {
      return getFieldByteValue(4, index, Fit.SUBFIELD_INDEX_MAIN_FIELD);
   }

   /**
    * Set data field
    *
    * @param index of data
    * @param data
    */
   public void setData(int index, Byte data) {
      setFieldValue(4, index, data, Fit.SUBFIELD_INDEX_MAIN_FIELD);
   }

}
