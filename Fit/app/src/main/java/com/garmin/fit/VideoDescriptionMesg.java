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


public class VideoDescriptionMesg extends Mesg {

   protected static final	Mesg videoDescriptionMesg;
   static {         
      // video_description   
      videoDescriptionMesg = new Mesg("video_description", MesgNum.VIDEO_DESCRIPTION);
      videoDescriptionMesg.addField(new Field("message_index", 254, 132, 1, 0, "", false));
      
      videoDescriptionMesg.addField(new Field("message_count", 0, 132, 1, 0, "", false));
      
      videoDescriptionMesg.addField(new Field("text", 1, 7, 1, 0, "", false));
      
   }

   public VideoDescriptionMesg() {
      super(Factory.createMesg(MesgNum.VIDEO_DESCRIPTION));
   }

   public VideoDescriptionMesg(final Mesg mesg) {
      super(mesg);
   }


   /**
    * Get message_index field
    * Comment: Long descriptions will be split into multiple parts
    *
    * @return message_index
    */
   public Integer getMessageIndex() {
      return getFieldIntegerValue(254, 0, Fit.SUBFIELD_INDEX_MAIN_FIELD);
   }

   /**
    * Set message_index field
    * Comment: Long descriptions will be split into multiple parts
    *
    * @param messageIndex
    */
   public void setMessageIndex(Integer messageIndex) {
      setFieldValue(254, 0, messageIndex, Fit.SUBFIELD_INDEX_MAIN_FIELD);
   }

   /**
    * Get message_count field
    * Comment: Total number of description parts
    *
    * @return message_count
    */
   public Integer getMessageCount() {
      return getFieldIntegerValue(0, 0, Fit.SUBFIELD_INDEX_MAIN_FIELD);
   }

   /**
    * Set message_count field
    * Comment: Total number of description parts
    *
    * @param messageCount
    */
   public void setMessageCount(Integer messageCount) {
      setFieldValue(0, 0, messageCount, Fit.SUBFIELD_INDEX_MAIN_FIELD);
   }

   /**
    * Get text field
    *
    * @return text
    */
   public String getText() {
      return getFieldStringValue(1, 0, Fit.SUBFIELD_INDEX_MAIN_FIELD);
   }

   /**
    * Set text field
    *
    * @param text
    */
   public void setText(String text) {
      setFieldValue(1, 0, text, Fit.SUBFIELD_INDEX_MAIN_FIELD);
   }

}
