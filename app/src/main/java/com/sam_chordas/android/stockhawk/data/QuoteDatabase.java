package com.sam_chordas.android.stockhawk.data;

import net.simonvt.schematic.annotation.Database;
import net.simonvt.schematic.annotation.Table;

/**
 * Setting up database and version
 */
@Database(version = QuoteDatabase.VERSION)
public class QuoteDatabase {
  private QuoteDatabase(){}

  //The DB version is 1
  public static final int VERSION = 1;

  @Table(QuoteColumns.class) public static final String QUOTES = "quotes";
}
