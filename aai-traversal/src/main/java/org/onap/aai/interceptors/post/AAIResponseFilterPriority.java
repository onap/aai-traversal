package org.onap.aai.interceptors.post;

public final class AAIResponseFilterPriority {
	
	private AAIResponseFilterPriority() {}

	public static final int HEADER_MANIPULATION = 1000;

	public static final int RESPONSE_TRANS_LOGGING = 2000;

	public static final int RESET_LOGGING_CONTEXT = 3000;

}
