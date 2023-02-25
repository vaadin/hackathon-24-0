package com.example.application.views.dialog;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;

import com.example.application.entity.Invoice;
import com.example.application.entity.Item;
import com.example.application.entity.Product;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.gridpro.GridPro;
import com.vaadin.flow.component.gridpro.GridProVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
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
	private ComboBox<Item> comboBox = new ComboBox<>();
	private static Map<Integer, Double> priceMap = new HashMap<>();

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

		HorizontalLayout productLayout = new HorizontalLayout();
		Button addButton = new Button("", new Icon(VaadinIcon.PLUS_SQUARE_O));
		addButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		addButton.setTooltipText("click to add new product");
		productLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
		productLayout.add(productGrid, addButton);
		productLayout.setWidthFull();
		dialogMainLayout.add(customerDetails, productLayout, generateInvoice);
		add(dialogMainLayout);

		generateInvoice.addClickListener(event -> {
			generateInvoiceSheet(name.getValue(), email.getValue(), phNumber.getValue(), address.getValue());
			this.close();
		});

		addButton.addClickListener(event -> {
			productList.add(new Product());
			productGrid.setItems(productList);
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

	public void generateInvoiceSheet(String name, String email, String phNumber, String address) {
		spreadsheet.createNewSheet(name, 100, 50);
		spreadsheet.setColumnWidth(1, 150);
		spreadsheet.setColumnWidth(2, 180);

		CellStyle cellStyle = spreadsheet.getWorkbook().createCellStyle();
		Font font = spreadsheet.getWorkbook().createFont();
		font.setBold(true);
		cellStyle.setFont(font);
		Cell cell = spreadsheet.createCell(1, 1, "Invoice Number: ");
		cell.setCellStyle(cellStyle);
		//spreadsheet.refreshCells(cell);
		spreadsheet.createCell(1, 2, generateInvoiceNumber(name));
		cell = spreadsheet.createCell(2, 1, "Customer Name: ");
		cell.setCellStyle(cellStyle);
		//spreadsheet.refreshCells(cell);
		spreadsheet.createCell(2, 2, name);
		cell = spreadsheet.createCell(3, 1, "Phone Number: ");
		cell.setCellStyle(cellStyle);
		//spreadsheet.refreshCells(cell);
		spreadsheet.createCell(3, 2, phNumber);
		cell = spreadsheet.createCell(4, 1, "Address: ");
		cell.setCellStyle(cellStyle);
		spreadsheet.refreshCells(cell);
		spreadsheet.createCell(4, 2, address);
		
		
		CellStyle mergedCellStyle = spreadsheet.getWorkbook().createCellStyle();
		font = spreadsheet.getWorkbook().createFont();
		font.setBold(true);
		font.setColor(IndexedColors.WHITE.getIndex());
		mergedCellStyle.setFont(font);
		mergedCellStyle.setFillBackgroundColor(IndexedColors.BLUE_GREY.getIndex());
		cell = spreadsheet.createCell(7, 1, "Products");
		cell.setCellStyle(mergedCellStyle);
		spreadsheet.refreshCells(cell);
		spreadsheet.addMergedRegion(7, 1, 7, 4);
		
		CellStyle headerCellStyle = spreadsheet.getWorkbook().createCellStyle();
		font = spreadsheet.getWorkbook().createFont();
		font.setBold(true);
		headerCellStyle.setFont(font);
		headerCellStyle.setFillBackgroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
		
		cell = spreadsheet.createCell(8, 1, "Product Name");
		cell.setCellStyle(headerCellStyle);
		//spreadsheet.refreshCells(cell);
		cell = spreadsheet.createCell(8, 2, "Price per Item");
		cell.setCellStyle(headerCellStyle);
		//spreadsheet.refreshCells(cell);
		cell = spreadsheet.createCell(8, 3, "Quantity");
		cell.setCellStyle(headerCellStyle);
		//spreadsheet.refreshCells(cell);
		cell = spreadsheet.createCell(8, 4, "Price");
		cell.setCellStyle(headerCellStyle);
		spreadsheet.refreshCells(cell);
		int row = 9;
		int col = 1;
		Double totalPrice = 0.0;
		for (Product p : productList) {
			spreadsheet.createCell(row, col, p.getName());
			col++;
			spreadsheet.createCell(row, col, p.getPrice());
			col++;
			spreadsheet.createCell(row, col, p.getQuantity());
			col++;
			Double price = Double.valueOf(p.getQuantity()) * Double.valueOf(p.getPrice());
			spreadsheet.createCell(row, col, price);
			col++;
			row++;
			col = 1;
			totalPrice = totalPrice + price;
		}

		row++;
		cell = spreadsheet.createCell(row, 1, "TotalPrice");
		cell.setCellStyle(cellStyle);
		spreadsheet.createCell(row, 4, totalPrice);

		includeTotalPriceToChartData(totalPrice);
		spreadsheet.setActiveSheetIndex(1);
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

	public void includeTotalPriceToChartData(Double totalPrice) {
		spreadsheet.setActiveSheetIndex(1);
		// int hour = LocalTime.now().getHour();
		Random random = new Random();
		int hour = random.nextInt(8, 16);

		Double price = priceMap.compute(hour, (k, v) -> v == null ? totalPrice : v + totalPrice);

		if (hour >= 8 && hour <= 16) {
			String cell = "B" + (hour - 6);
			spreadsheet.getCell(cell).setCellValue(price);
		}
	}

}
