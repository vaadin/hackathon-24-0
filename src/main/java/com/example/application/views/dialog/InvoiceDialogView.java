package com.example.application.views.dialog;

import java.util.ArrayList;
import java.util.List;

import com.example.application.entity.Product;
import com.vaadin.flow.component.button.Button;
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
import com.vaadin.flow.theme.lumo.LumoUtility;

public class InvoiceDialogView extends Dialog {
	private Spreadsheet spreadsheet;
	private List<Product> productList = new ArrayList<Product>();
	private GridPro<Product> productGrid = new GridPro<>();
	private Binder<Product> productBinder;

	public InvoiceDialogView() {
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
		
		dialogMainLayout.add(customerDetails, productGrid ,generateInvoice);
		add(dialogMainLayout);

		generateInvoice.addClickListener(event -> {
			spreadsheet.createNewSheet("Invoice", 100, 50);
			spreadsheet.setActiveSheetIndex(1);
			spreadsheet.createCell(1, 1, name.getValue());
			this.close();
		});
		setWidth("40%");
	}

	private void createProductGrid() {
		productList.add(new Product());
		productGrid.setEditOnClick(true);
		productGrid.setEnterNextRow(true);
		productGrid.addThemeVariants(GridProVariant.LUMO_HIGHLIGHT_READ_ONLY_CELLS);
		productGrid.addEditColumn(Product::getName).text(Product::setName).setHeader("Product Name");
		productGrid.addEditColumn(Product::getQuantity).text(Product::setQuantity).setHeader("Quantity");
		productGrid.addColumn(Product::getPrice).setHeader("Price per Item");
		productGrid.addColumn(Product::getTotalPrice).setHeader("Total Price");
		productGrid.setItems(productList);
		
	}
	
	public Spreadsheet getSpreadsheet() {
		return spreadsheet;
	}

	public void setSpreadsheet(Spreadsheet spreadsheet) {
		this.spreadsheet = spreadsheet;
	}

}
