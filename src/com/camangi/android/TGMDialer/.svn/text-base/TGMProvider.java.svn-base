package com.camangi.android.TGMDialer;


import java.io.IOException;
import java.net.URI;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import com.android.internal.util.XmlUtils;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Environment;
import android.provider.BaseColumns;
import android.provider.Contacts;
import android.util.Log;
import android.util.Xml;

public class TGMProvider  extends ContentProvider{
	
	private static final String TAG = "TGMAPNProvider";
	
	public static final String DATABASE_NAME = "tgm_apns.db";
	
	public static final String CARRIERS_TABLE = "carriers";
	
	public static final int DATABASE_VERSION = 1;
	
	public static final String AUTHORITY = "tgm";
	
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
	
	private static final UriMatcher sUriMatcher;

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		
		
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		
		return null;
	}

	@Override
	public boolean onCreate() {
		
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		
		return 0;
	}
	
	
	private static class DatabaseHelper extends SQLiteOpenHelper {
        // Context to access resources with
        private Context mContext;

        /**
         * DatabaseHelper helper class for loading apns into a database.
         *
         * @param parser the system-default parser for apns.xml
         * @param confidential an optional parser for confidential APNS (stored separately)
         */
        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, getVersion(context));
            mContext = context;
        }

        private static int getVersion(Context context) {
            // Get the database version, combining a static schema version and the XML version
            Resources r = context.getResources();
            
            XmlResourceParser parser = r.getXml(com.android.internal.R.xml.apns);
            
            try {
            	
                XmlUtils.beginDocument(parser, "apns");
                
                int publicversion = Integer.parseInt(parser.getAttributeValue(null, "version"));
                
                return DATABASE_VERSION | publicversion;
                
            } catch (Exception e) {
            	
                Log.e(TAG, "Can't get version of APN database", e);
                
                return DATABASE_VERSION;
                
            } finally {
                parser.close();
            }
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // Set up the database schema
            db.execSQL("CREATE TABLE " + CARRIERS_TABLE +
                "(_id INTEGER PRIMARY KEY," +
                    "name TEXT," +
                    "numeric TEXT," +
                    "mcc TEXT," +
                    "mnc TEXT," +
                    "apn TEXT," +
                    "user TEXT," +
                    "server TEXT," +
                    "password TEXT," +
                    "proxy TEXT," +
                    "port TEXT," +
                    "mmsproxy TEXT," +
                    "mmsport TEXT," +
                    "mmsc TEXT," +
                    "type TEXT," +
                    "current INTEGER);");

            initDatabase(db);
        }

        private void initDatabase(SQLiteDatabase db) {
            // Read internal APNS data
            Resources r = mContext.getResources();
            
            XmlResourceParser parser = r.getXml(com.android.internal.R.xml.apns);
            
            int publicversion = -1;
            
            try {
            	
                XmlUtils.beginDocument(parser, "apns");
                
                publicversion = Integer.parseInt(parser.getAttributeValue(null, "version"));
                
                loadApns(db, parser);
                
            } catch (Exception e) {
            	
                Log.e(TAG, "Got exception while loading APN database.", e);
                
            } finally {
            	
                parser.close();
                
            }

//            // Read external APNS data (partner-provided)
//            XmlPullParser confparser = null;
//            // Environment.getRootDirectory() is a fancy way of saying ANDROID_ROOT or "/system".
//            File confFile = new File(Environment.getRootDirectory(), PARTNER_APNS_PATH);
//            FileReader confreader = null;
//            
//            try {
//                confreader = new FileReader(confFile);
//                confparser = Xml.newPullParser();
//                confparser.setInput(confreader);
//                XmlUtils.beginDocument(confparser, "apns");
//
//                // Sanity check. Force internal version and confidential versions to agree
//                int confversion = Integer.parseInt(confparser.getAttributeValue(null, "version"));
//                if (publicversion != confversion) {
//                    throw new IllegalStateException("Internal APNS file version doesn't match "
//                            + confFile.getAbsolutePath());
//                }
//
//                loadApns(db, confparser);
//            } catch (FileNotFoundException e) {
//                // It's ok if the file isn't found. It means there isn't a confidential file
//                // Log.e(TAG, "File not found: '" + confFile.getAbsolutePath() + "'");
//            } catch (Exception e) {
//                Log.e(TAG, "Exception while parsing '" + confFile.getAbsolutePath() + "'", e);
//            } finally {
//                try { if (confreader != null) confreader.close(); } catch (IOException e) { }
//            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + CARRIERS_TABLE + ";");
            onCreate(db);
        }

        /**
         * Gets the next row of apn values.
         *
         * @param parser the parser
         * @return the row or null if it's not an apn
         */
        private ContentValues getRow(XmlPullParser parser) {
            if (!"apn".equals(parser.getName())) {
                return null;
            }

            ContentValues map = new ContentValues();

            String mcc = parser.getAttributeValue(null, "mcc");
            String mnc = parser.getAttributeValue(null, "mnc");
            String numeric = mcc + mnc;

            map.put(Carriers.NUMERIC,numeric);
            map.put(Carriers.MCC, mcc);
            map.put(Carriers.MNC, mnc);
            map.put(Carriers.NAME, parser.getAttributeValue(null, "carrier"));
            map.put(Carriers.APN, parser.getAttributeValue(null, "apn"));
            map.put(Carriers.USER, parser.getAttributeValue(null, "user"));
            map.put(Carriers.SERVER, parser.getAttributeValue(null, "server"));
            map.put(Carriers.PASSWORD, parser.getAttributeValue(null, "password"));

            // do not add NULL to the map so that insert() will set the default value
            String proxy = parser.getAttributeValue(null, "proxy");
            
            if (proxy != null) {
                map.put(Carriers.PROXY, proxy);
            }
            
            String port = parser.getAttributeValue(null, "port");
            
            if (port != null) {
                map.put(Carriers.PORT, port);
            }
            
            String mmsproxy = parser.getAttributeValue(null, "mmsproxy");
            
            if (mmsproxy != null) {
                map.put(Carriers.MMSPROXY, mmsproxy);
            }
            
            String mmsport = parser.getAttributeValue(null, "mmsport");
            
            if (mmsport != null) {
                map.put(Carriers.MMSPORT, mmsport);
            }
            
            map.put(Carriers.MMSC, parser.getAttributeValue(null, "mmsc"));
            
            String type = parser.getAttributeValue(null, "type");
            
            if (type != null) {
                map.put(Carriers.TYPE, type);
            }

            return map;
        }

        /*
         * Loads apns from xml file into the database
         *
         * @param db the sqlite database to write to
         * @param parser the xml parser
         *
         */
        private void loadApns(SQLiteDatabase db, XmlPullParser parser) {
            
        	if (parser != null) {
                try {
                    while (true) {
                        
                    	XmlUtils.nextElement(parser);
                        
                    	ContentValues row = getRow(parser);
                        
                        if (row != null) {
                        	
                            db.insert(CARRIERS_TABLE, null, row);
                            
                        } else {
                            break;  // do we really want to skip the rest of the file?
                        }
                    }
                } catch (XmlPullParserException e)  {
                   
                	Log.e(TAG, "Got execption while getting perferred time zone.", e);
                	
                } catch (IOException e) {
                   
                	Log.e(TAG, "Got execption while getting perferred time zone.", e);
                    
                }
            }
        }
    }
	
	public static final class Carriers implements BaseColumns {
        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI =
            Uri.parse("content://tgm/carriers");

        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "name ASC";

        public static final String NAME = "name";

        public static final String APN = "apn";

        public static final String PROXY = "proxy";

        public static final String PORT = "port";

        public static final String MMSPROXY = "mmsproxy";

        public static final String MMSPORT = "mmsport";

        public static final String SERVER = "server";

        public static final String USER = "user";

        public static final String PASSWORD = "password";

        public static final String MMSC = "mmsc";

        public static final String MCC = "mcc";

        public static final String MNC = "mnc";

        public static final String NUMERIC = "numeric";

        public static final String TYPE = "type";

        public static final String CURRENT = "current";
    }
	
	
	private static final int APN = 1;
	private static final int APN_ID = 2;
	
	static {
		
		sUriMatcher =new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(AUTHORITY, "apns", APN);
		sUriMatcher.addURI(AUTHORITY, "apns/#", APN_ID);
	}

}
