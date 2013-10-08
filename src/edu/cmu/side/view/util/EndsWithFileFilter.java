package edu.cmu.side.view.util;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class EndsWithFileFilter extends FileFilter
{
	String[] extensions;
	String description;
	
	
	public EndsWithFileFilter(String description, String... ext)
	{
		super();
		this.extensions = ext;
		this.description = description;
	}
	
	public String[] getExtensions()
	{
		return extensions;
	}

	@Override
	public boolean accept(File file)
	{
		String fileName = file.getName();
		for(String ext : extensions)
		{
			if(fileName.toLowerCase().endsWith(ext.toLowerCase()))
				return true;
		}
		return false;
	}

	@Override
	public String getDescription()
	{
		return description;
	}
	
}