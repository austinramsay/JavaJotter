package com.austinramsay.javajotter;

import com.austinramsay.javajotterlibrary.*;

public class Builder {
	public static Authenticator buildAuthenticator(String username, char[] password, String serverAddress) {
		return new Authenticator(
				username,
				password,
				serverAddress);
	}

	public static Request buildSyncRequest() {
		return Request.USER_CONTENT;
	}
}