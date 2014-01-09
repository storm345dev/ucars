package com.useful.ucars;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ListStore {

	public File StorageFile;
	public ArrayList<String> values;

	public ListStore(File file) {
		this.StorageFile = file;
		this.values = new ArrayList<String>();

		if (this.StorageFile.exists() == false) {
			try {
				// this.StorageFile.mkdir();
				this.StorageFile.createNewFile();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void load() {
		try {
			DataInputStream input = new DataInputStream(new FileInputStream(
					this.StorageFile));
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					input));

			String line;

			while ((line = reader.readLine()) != null) {
				this.values.add(line);
			}

			reader.close();
			input.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void save() {

		try {
			FileWriter stream = new FileWriter(this.StorageFile);
			BufferedWriter out = new BufferedWriter(stream);

			for (String value : this.values) {
				out.write(value);
				out.newLine();
			}

			out.close();
			stream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public Boolean contains(String value) {
		return this.values.contains(value);
	}

	public void add(String value) {
		this.values.add(value);
	}

	public void remove(String value) {
		this.values.remove(value);
	}

	public ArrayList<String> getValues() {
		return this.values;
	}

}
