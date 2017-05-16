package com.att.scef.scef;

public class SCEFRangeException extends Exception {
	  private static final long serialVersionUID = 1L;

	  /**
	   * Default constructor
	   */
	  public SCEFRangeException() {
	    super();
	  }

	  /**
	   * Constructor with reason string
	   * @param message reason string
	   */
	  public SCEFRangeException(String message) {
	    super(message);
	  }

	  /**
	   * Constructor with reason string and parent exception
	   * @param message message reason string
	   * @param cause parent exception
	   */
	  public SCEFRangeException(String message, Throwable cause) {
	    super(message, cause);
	  }

	  /**
	   * Constructor with parent exception
	   * @param cause  parent exception
	   */
	  public SCEFRangeException(Throwable cause) {
	    super(cause);
	  }
}
