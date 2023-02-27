package com.example.application.views.spreadsheet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.ss.usermodel.Cell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.application.views.dialog.InvoiceDialogView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.SpreadsheetComponentFactory;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;

@Route("shop")
public class SpreadsheetViewSudebi extends VerticalLayout {
	private Button newInvoice = new Button("Create New Invoice");
	private Button export = new Button("Export");
	private InvoiceDialogView dialog;
	private Spreadsheet spreadsheet;

	public SpreadsheetViewSudebi() throws IOException {

		InputStream stream = getClass().getResourceAsStream("/testsheets/shop.xlsx");

		spreadsheet = new Spreadsheet(stream);
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

				if (spreadsheet.getActiveSheetIndex() == 0 && rowIndex == 2 && columnIndex == 4) {
					export.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
					return export;
				}
				return null;
			}
		});
		spreadsheet.setColumnWidth(1, 200);
		spreadsheet.setColumnWidth(4, 100);

		newInvoice.addClickListener(event -> {
			// Notification.show("New Invoice", 3000, Position.MIDDLE);
			dialog = new InvoiceDialogView();
			dialog.setSpreadsheet(spreadsheet);
			dialog.open();
		});

		export.addClickListener(event -> {
			downloadSpreadsheetFile();
		});

	}

	private Logger getLogger() {
		return LoggerFactory.getLogger(getClass());
	}

	private void downloadSpreadsheetFile() {
		try {
			final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			spreadsheet.write(outputStream);
			final StreamResource resource = new StreamResource("file.xlsx",
					() -> new ByteArrayInputStream(outputStream.toByteArray()));
			final StreamRegistration registration = VaadinSession.getCurrent().getResourceRegistry()
					.registerResource(resource);
			UI.getCurrent().getPage().open(registration.getResourceUri().toString());
		} catch (Exception e) {
			getLogger().warn("Error while processing the file to download", e);
		}
	}
}
