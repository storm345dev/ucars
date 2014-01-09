package com.useful.ucars;

public class Colors {
	private String success = "";
	private String error = "";
	private String info = "";
	private String title = "";
	private String tp = "";

	public Colors(String success, String error, String info, String title,
			String tp) {
		this.success = ucars.colorise(success);
		this.error = ucars.colorise(error);
		this.info = ucars.colorise(info);
		this.title = ucars.colorise(title);
		this.tp = ucars.colorise(tp);
	}

	public String getSuccess() {
		return this.success;
	}

	public String getError() {
		return this.error;
	}

	public String getInfo() {
		return this.info;
	}

	public String getTitle() {
		return this.title;
	}

	public String getTp() {
		return this.tp;
	}
}
