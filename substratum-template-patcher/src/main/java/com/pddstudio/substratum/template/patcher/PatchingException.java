package com.pddstudio.substratum.template.patcher;

/**
 * Created by pddstudio on 25/04/2017.
 */

public class PatchingException extends Exception {

	public PatchingException(Exception e) {
		super(e);
	}

	public PatchingException(String message) {
		super(message);
	}

}
