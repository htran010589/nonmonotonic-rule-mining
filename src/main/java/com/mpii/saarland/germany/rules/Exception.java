package com.mpii.saarland.germany.rules;

public class Exception {

	private String exception;

	private ExceptionType type;

	public Exception(String exception, ExceptionType type) {
		this.exception = exception;
		this.type = type;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(exception);
		if (type == ExceptionType.FIRST) {
			result.append("(x)");
		} else if (type == ExceptionType.SECOND) {
			result.append("(z)");
		} else {
			result.append("(x, z)");
		}
		return result.toString();
	}

	public String getException() {
		return exception;
	}

	public void setException(String exception) {
		this.exception = exception;
	}

	public ExceptionType getType() {
		return type;
	}

	public void setType(ExceptionType type) {
		this.type = type;
	}

	@Override
	public int hashCode() {
		return (exception + "\t" + type).hashCode();
	}

	@Override
	public boolean equals(Object that) {
		if (this == that) {
			return true;
		}
		if (that == null) {
			return false;
		}
		if (getClass() != that.getClass()) {
			return false;
		}
		Exception other = (Exception) that;
		if (!exception.equals(other.exception) || type != other.type) {
			return false;
		}
		return true;
	}

}
