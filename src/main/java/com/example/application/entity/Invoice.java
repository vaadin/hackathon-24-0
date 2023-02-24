package com.example.application.entity;

import java.util.List;

public class Invoice {
	private String name;
	private String email;
	private String phNumber;
	private String Address;
	private List<Product> productLists;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhNumber() {
		return phNumber;
	}

	public void setPhNumber(String phNumber) {
		this.phNumber = phNumber;
	}

	public String getAddress() {
		return Address;
	}

	public void setAddress(String address) {
		Address = address;
	}

	public List<Product> getProductLists() {
		return productLists;
	}

	public void setProductLists(List<Product> productLists) {
		this.productLists = productLists;
	}

}
