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

public enum CoursePoint {
   GENERIC((short)0),
   SUMMIT((short)1),
   VALLEY((short)2),
   WATER((short)3),
   FOOD((short)4),
   DANGER((short)5),
   LEFT((short)6),
   RIGHT((short)7),
   STRAIGHT((short)8),
   FIRST_AID((short)9),
   FOURTH_CATEGORY((short)10),
   THIRD_CATEGORY((short)11),
   SECOND_CATEGORY((short)12),
   FIRST_CATEGORY((short)13),
   HORS_CATEGORY((short)14),
   SPRINT((short)15),
   LEFT_FORK((short)16),
   RIGHT_FORK((short)17),
   MIDDLE_FORK((short)18),
   SLIGHT_LEFT((short)19),
   SHARP_LEFT((short)20),
   SLIGHT_RIGHT((short)21),
   SHARP_RIGHT((short)22),
   U_TURN((short)23),
   INVALID((short)255);


   protected short value;




   private CoursePoint(short value) {
     this.value = value;
   }

   public static CoursePoint getByValue(final Short value) {
      for (final CoursePoint type : CoursePoint.values()) {
         if (value == type.value)
            return type;
      }

      return CoursePoint.INVALID;
   }

   public short getValue() {
      return value;
   }


}
