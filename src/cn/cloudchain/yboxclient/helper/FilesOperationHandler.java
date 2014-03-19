package cn.cloudchain.yboxclient.helper;

public abstract class FilesOperationHandler<T> extends WeakHandler<T> {
	public static final String BUNDLE_FILES = "files";
	public static final int REQUEST_COMPLETE = 0;
	public static final int FILE_LOAD_SUCESS = 1;
	public static final int FILE_DELETE_SUCCESS = 2;
	public static final int FILE_ADD_SUCCESS = 3;
	
	public FilesOperationHandler(T owner) {
		super(owner);
	}

}
