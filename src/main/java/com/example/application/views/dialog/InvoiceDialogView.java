package com.example.application.views.dialog;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.example.application.entity.Invoice;
import com.example.application.entity.Item;
import com.example.application.entity.Product;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.gridpro.GridPro;
import com.vaadin.flow.component.gridpro.GridProVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.theme.lumo.LumoUtility;

public class InvoiceDialogView extends Dialog {
	private Spreadsheet spreadsheet;
	private List<Product> productList = new ArrayList<Product>();
	private GridPro<Product> productGrid = new GridPro<>();
	private Binder<Invoice> productBinder;
	private List<Item> itemsList = new ArrayList<Item>();
	ComboBox<Item> comboBox = new ComboBox<>();

	public InvoiceDialogView() {
		populateItemsList();
		VerticalLayout dialogMainLayout = new VerticalLayout();

		HorizontalLayout customerDetails = new HorizontalLayout();
		Div customerNamePhEmail = new Div();
		customerNamePhEmail.addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN,
				LumoUtility.JustifyContent.CENTER, LumoUtility.AlignItems.CENTER);
		Div customerAddress = new Div();
		customerAddress.addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN,
				LumoUtility.JustifyContent.START, LumoUtility.AlignItems.CENTER);

		TextField name = new TextField();
		name.setLabel("Customer Name");
		EmailField email = new EmailField();
		email.setLabel("Email Id");
		Button generateInvoice = new Button("Generate Invoice");
		TextField phNumber = new TextField();
		phNumber.setLabel("Phone Number");
		customerNamePhEmail.add(name, email, phNumber);

		TextArea address = new TextArea();
		address.setLabel("Address");
		customerAddress.add(address);

		customerDetails.add(customerNamePhEmail, customerAddress);

		createProductGrid();

		dialogMainLayout.add(customerDetails, productGrid, generateInvoice);
		add(dialogMainLayout);

		generateInvoice.addClickListener(event -> {
			generateInvoiceSheet(name.getValue(),email.getValue(),phNumber.getValue(),address.getValue());
			this.close();
		});
		setWidth("40%");
		/*
		 * 
		 * productBinder.forField(name).bind(Invoice::getName,Invoice::setName);
		 * productBinder.forField(email).bind(Invoice::getEmail,Invoice::setEmail);
		 * productBinder.forField(phNumber).bind(Invoice::getPhNumber,Invoice::
		 * setPhNumber);
		 * productBinder.forField(address).bind(Invoice::getAddress,Invoice::setAddress)
		 * ; productBinder.forField(productGrid).bind(Invoice::getProductLists,Invoice::
		 * setProductLists);
		 */
	}

	private void createProductGrid() {
		productList.add(new Product());
		productGrid.setEditOnClick(true);
		productGrid.setEnterNextRow(true);
		productGrid.addThemeVariants(GridProVariant.LUMO_HIGHLIGHT_READ_ONLY_CELLS);
		// productGrid.addEditColumn(Product::getName, new ComponentRenderer<>(item ->
		// {return this.comboBox}));
		productGrid.addEditColumn(Product::getName).text(Product::setName).setHeader("Name");
		productGrid.addEditColumn(Product::getQuantity).text(Product::setQuantity).setHeader("Quantity");
		productGrid.addEditColumn(Product::getPrice).text(Product::setPrice).setHeader("Price per Item");
		productGrid.addColumn(Product::getTotalPrice).setHeader("Total Price");
		productGrid.setItems(productList);

	}

	private static Renderer<Product> addComboBox() {
		return LitRenderer.<Product>of(
				"<vaadin-combo-box item-value-path=\"name\" .items=\"${this.itemsList}\"></vaadin-combo-box>");
	}

	public void populateItemsList() {
		Item newItem1 = new Item();
		newItem1.setName("Product1");
		newItem1.setPrice("500");

		Item newItem2 = new Item();
		newItem2.setName("Product2");
		newItem2.setPrice("400");
		Item newItem3 = new Item();
		newItem3.setName("Product3");
		newItem3.setPrice("300");
		itemsList.add(newItem1);
		itemsList.add(newItem2);
		itemsList.add(newItem3);

		comboBox.setItems(itemsList);
	}
	
	public void generateInvoiceSheet(String name,String email,String phNumber,String address){
		spreadsheet.createNewSheet(name, 100, 50);
		spreadsheet.setActiveSheetIndex(1);
		spreadsheet.createCell(1, 1, "Invoice Number: ");
		spreadsheet.createCell(1, 2, generateInvoiceNumber(name));
		spreadsheet.createCell(2, 1, "Customer Name: ");
		spreadsheet.createCell(2, 2, name);
		spreadsheet.createCell(3, 1, "Phone Number: ");
		spreadsheet.createCell(3, 2, phNumber);
		spreadsheet.createCell(4, 1, "Address: ");
		spreadsheet.createCell(4, 2, address);

		spreadsheet.createCell(5, 1, "Products");
		spreadsheet.createCell(6, 1, "Product Name");
		spreadsheet.createCell(6, 2, "Price per Item");
		spreadsheet.createCell(6, 3, "Quantity");
		spreadsheet.createCell(6, 4, "Price");
		int row=7;
		int col=1;
		for (Product p : productList) {
			spreadsheet.createCell(row, col, p.getName());
			col++;
			spreadsheet.createCell(row, col, p.getPrice());
			col++;
			spreadsheet.createCell(row, col, p.getQuantity());
			col++;
			spreadsheet.createCell(row, col, (Double.valueOf(p.getQuantity()) * Double.valueOf(p.getPrice())));
			col++;
          
		}
	}

	public Spreadsheet getSpreadsheet() {
		return spreadsheet;
	}

	public void setSpreadsheet(Spreadsheet spreadsheet) {
		this.spreadsheet = spreadsheet;
	}
	
	public String generateInvoiceNumber(String name) {
		    Date date = new Date();
		    return name.concat(String.valueOf(date.getTime()));
	}

}
