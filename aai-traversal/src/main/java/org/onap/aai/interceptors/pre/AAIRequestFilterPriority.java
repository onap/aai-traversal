package org.onap.aai.interceptors.pre;

public final class AAIRequestFilterPriority {
	
	private AAIRequestFilterPriority() {}
	
	public static final int REQUEST_TRANS_LOGGING = 1000;
	
	public static final int HEADER_VALIDATION = 2000;

	public static final int SET_LOGGING_CONTEXT = 3000;

	public static final int AUTHORIZATION = 4000;

	public static final int HEADER_MANIPULATION = 5000;

	public static final int REQUEST_MODIFICATION = 6000;
}
