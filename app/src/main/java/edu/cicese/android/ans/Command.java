package edu.cicese.android.ans;

public class Command {
	//PACKAGE COMMANDS
	public final static int QUERY = 101;
	public final static int SEND_IMG = 102;
	public final static int TAGS_FOUND = 103;
	public final static int USER_LOC = 104;
	public final static int IMG_ACK = 105;

	public final static int CHK_REP = 111;
	public final static int SEND_REP = 112;

	public final static int TAG_TREE = 121;
	public final static int SEND_TAGS = 122;
	public final static int FILE_INFO = 123;
	public final static int SEND_TAG_FILE = 124;
	public final static int TAG_FILE_ACK = 125;
	public final static int REP_SYNCD = 126;

	/*public final static int FIND_TAGS = 101;
	public final static int SEND_IMG = 102;
	public final static int TAGS_FOUND = 103;

	public final static int LOC_USER = 104;
	public final static int SEND_IMG_LOC = 105;
	public final static int IMG_LOC = 106;
	public final static int IMG_ACK = 108;
	public final static int USER_LOC = 115;

//	public final static int SEND_REP_VER = 107;
//	public final static int REP_VER = 108;
	public final static int CHK_REP = 107;
	public final static int SEND_REP = 116;
	
	public final static int TAG_TREE = 109;
//	public final static int ENT_TAG_TREE = 117;
	public final static int SEND_TAGS = 110;
	public final static int FILE_INFO = 111;
	public final static int SEND_TAG_FILE = 112;
	public final static int TAG_FILE_ACK = 113;
	public final static int REP_SYNCD = 114;*/

	//SPACE AND TAG COMMANDS
	public final static int CREATED = 301;
	public final static int DELETED = 302;
	public final static int MODIFIED = 303;
	public final static int NO_ACTION = 304;

	//MESSAGE TYPE
	public final static int MSG_TAG = 1;
	public final static int MSG_INFO = 2;

	//NOTIFICATION TYPE
	public final static int TYPE_VIBRATION = 1;
	public final static int TYPE_SOUND = 2;
	public final static int TYPE_BOTH = 3;
}