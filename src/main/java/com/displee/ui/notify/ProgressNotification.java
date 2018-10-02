package com.displee.ui.notify;

import javafx.application.Preloader;
import lombok.Getter;

public class ProgressNotification extends Preloader.ProgressNotification {

	/**
	 * A notification message.
	 */
	@Getter
	private String message;

	public ProgressNotification(double progress, String message) {
		super(progress);
		this.message = message;
	}

}
