package com.happy.live.entity;


public class TURL {
	public String url;
	public String name;
	public String icon;
	
	public TURL(String url ,String name, String icon) {
		this.url = url;
		this.name = name;
		this.icon = icon;
	}
	
	 @Override
	public boolean equals(Object o) {
		 if (o instanceof TURL) {
			 return url.equals(((TURL)o).url);
		 } else {
			 return false;
		 }
	}
}
