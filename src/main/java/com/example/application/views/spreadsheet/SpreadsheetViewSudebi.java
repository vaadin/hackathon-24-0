package com.example.application.views.spreadsheet;

import java.io.IOException;

import org.apache.poi.ss.usermodel.Cell;

import com.example.application.entity.Product;
import com.example.application.views.dialog.InvoiceDialogView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.SpreadsheetComponentFactory;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;

@Route("test")
public class SpreadsheetViewSudebi extends VerticalLayout {
	private Button newInvoice = new Button("Create New Invoice");
	private InvoiceDialogView dialog;

	
	public SpreadsheetViewSudebi() throws IOException {
		Spreadsheet spreadsheet = new Spreadsheet();
		spreadsheet.setHeight("400px");
		add(spreadsheet);
		setSizeFull();
		spreadsheet.setGridlinesVisible(false);
		spreadsheet.setSpreadsheetComponentFactory(new SpreadsheetComponentFactory() {
			@Override
			public Component getCustomEditorForCell(org.apache.poi.ss.usermodel.Cell cell, int rowIndex,
					int columnIndex, Spreadsheet spreadsheet, org.apache.poi.ss.usermodel.Sheet sheet) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void onCustomEditorDisplayed(org.apache.poi.ss.usermodel.Cell cell, int rowIndex, int columnIndex,
					Spreadsheet spreadsheet, org.apache.poi.ss.usermodel.Sheet sheet, Component customEditor) {
				// TODO Auto-generated method stub

			}

			@Override
			public Component getCustomComponentForCell(Cell cell, int rowIndex, int columnIndex,
					Spreadsheet spreadsheet, org.apache.poi.ss.usermodel.Sheet sheet) {
				if (spreadsheet.getActiveSheetIndex() == 0 && rowIndex == 2 && columnIndex == 1) {
					newInvoice.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
					return newInvoice;
				}
				return null;
			}
		});
		spreadsheet.setColumnWidth(1,200);

		
		
		newInvoice.addClickListener(event -> {
			//Notification.show("New Invoice", 3000, Position.MIDDLE);
			dialog = new InvoiceDialogView();
			dialog.setSpreadsheet(spreadsheet);
			dialog.open();
		});

	}

	public void createSpreadSheet() {
		Spreadsheet spreadsheet = new Spreadsheet();
		spreadsheet.setHeight("400px");
		add(spreadsheet);
		setSizeFull();
	}
}
