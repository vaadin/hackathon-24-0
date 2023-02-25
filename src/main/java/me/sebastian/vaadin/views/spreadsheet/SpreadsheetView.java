package me.sebastian.vaadin.views.spreadsheet;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.contextmenu.HasMenuItems;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.SpreadsheetComponentFactory;
import com.vaadin.flow.component.spreadsheet.SpreadsheetFilterTable;
import com.vaadin.flow.component.spreadsheet.SpreadsheetTable;
import com.vaadin.flow.component.upload.Receiver;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.lumo.LumoIcon;
import com.vaadin.flow.theme.lumo.LumoUtility;
import me.sebastian.vaadin.views.MainLayout;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.io.*;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

@PageTitle("Spreadsheet")
@Route(value = "spreadsheet", layout = MainLayout.class)
public class SpreadsheetView extends VerticalLayout implements Receiver {

    private final Spreadsheet spreadsheet;
    private File uploadedFile;
    private File previousFile;
    private H3 invoiceNumber;
    private Span invoiceSource;

    private final Set<Integer> primeNumbers;

    public SpreadsheetView() throws IOException, URISyntaxException {
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        InputStream stream = getClass().getResourceAsStream("/simple-invoice.xlsx");

        spreadsheet = new Spreadsheet(stream);
        spreadsheet.setHeight("400px");
        add(spreadsheet);

        primeNumbers = sieveOfEratosthenes(1000);
        spreadsheet.setSpreadsheetComponentFactory(new SpreadsheetComponentFactory() {
            @Override
            public Component getCustomComponentForCell(Cell cell, int rowIndex, int columnIndex, Spreadsheet spreadsheet, Sheet sheet) {
                return null;
            }

            @Override
            public Component getCustomEditorForCell(Cell cell, int rowIndex, int columnIndex, Spreadsheet spreadsheet, Sheet sheet) {
                if (spreadsheet.getActiveSheetIndex() == 0 && rowIndex == 4 && columnIndex == 3) {
                    return new DatePicker(cell.getLocalDateTimeCellValue().toLocalDate(),
                            event -> {
                                Instant instant = event.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant();
                                spreadsheet.refreshCells(spreadsheet.createCell(rowIndex, columnIndex, Date.from(instant)));
                            });
                }

                if (spreadsheet.getActiveSheetIndex() == 0 && rowIndex == 3 && columnIndex == 3) {
                    ComboBox<Integer> primeBox = new ComboBox<>();
                    primeBox.setItems(primeNumbers);
                    primeBox.setValue((int) cell.getNumericCellValue());
                    primeBox.addValueChangeListener(event -> {
                        Notification.show(String.format("%d selected", event.getValue()));
                        spreadsheet.refreshCells(spreadsheet.createCell(rowIndex, columnIndex, Double.valueOf(event.getValue())));
                    });
                    return primeBox;
                }
                return null;
            }

            @Override
            public void onCustomEditorDisplayed(Cell cell, int rowIndex, int columnIndex, Spreadsheet spreadsheet, Sheet sheet, Component customEditor) {

            }
        });

        add(createViewHeader(), spreadsheet);
    }

    private Logger getLogger() {
        return LoggerFactory.getLogger(getClass());
    }

    @Override
    public OutputStream receiveUpload(String fileName, String mimeType) {
        try {
            File file = new File(fileName);
            file.deleteOnExit();
            uploadedFile = file;
            return new FileOutputStream(uploadedFile);
        } catch (FileNotFoundException e) {
            getLogger().warn("ERROR reading file " + fileName, e);
        }
        return null;
    }

    private VerticalLayout createViewHeader() {
        VerticalLayout header = new VerticalLayout();
        header.setSpacing(false);
        header.addClassName(LumoUtility.Padding.Bottom.XSMALL);

        HorizontalLayout viewHeading = new HorizontalLayout();
        viewHeading.setWidthFull();
        viewHeading.setAlignItems(Alignment.BASELINE);
        viewHeading.addClassName(LumoUtility.Padding.Left.SMALL);
        invoiceNumber = new H3();
        invoiceNumber.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);
        invoiceSource = new Span();
        invoiceSource.addClassNames(LumoUtility.TextColor.SECONDARY);

        updateInvoiceNumberAndSource();

        viewHeading.add(invoiceNumber, invoiceSource);
        header.add(viewHeading, createMenuBar());

        return header;
    }

    private void updateInvoiceNumberAndSource() {
        invoiceNumber.setText("Invoice " + getStringValue(new CellReference("D4")));
        invoiceSource.setText("from " + getStringValue(new CellReference("A2")));
    }

    private String getStringValue(CellReference cellReference) {
        return spreadsheet.getDataFormatter().formatCellValue(spreadsheet.getCell(cellReference));
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        menuBar.addThemeVariants(MenuBarVariant.LUMO_TERTIARY);

        AtomicReference<CellRangeAddress> selectedCells = new AtomicReference<>();
        AtomicReference<CellRangeAddress> selectedCellMergedRegion = new AtomicReference<>();
        AtomicReference<CellReference> selectedCellReference = new AtomicReference<>();

        spreadsheet.addCellValueChangeListener(event -> {
            CellReference a2 = new CellReference(spreadsheet.getCell("A2"));
            CellReference d4 = new CellReference(spreadsheet.getCell("D4"));
            if (event.getChangedCells().contains(a2) || event.getChangedCells().contains(d4)) {
                updateInvoiceNumberAndSource();
            }

        });
        spreadsheet.addSelectionChangeListener(e -> {
            selectedCells.set(e.getCellRangeAddresses().stream().findFirst().orElse(null));
            selectedCellMergedRegion.set(e.getSelectedCellMergedRegion());
            selectedCellReference.set(e.getSelectedCellReference());
        });

        Dialog uploadFileDialog = createUploadDialog();

        MenuItem fileMenu = menuBar.addItem("File");
        SubMenu fileSubMenu = fileMenu.getSubMenu();
        createIconItem(fileSubMenu, LumoIcon.UPLOAD, "Import", "Import", e -> uploadFileDialog.open());
        createIconItem(fileSubMenu, LumoIcon.DOWNLOAD, "Export", "Export", e -> downloadSpreadsheetFile());

        MenuItem viewMenu = menuBar.addItem("View");
        SubMenu viewSubMenu = viewMenu.getSubMenu();
        createCheckableItem(viewSubMenu, "Grid lines", true,
                e -> spreadsheet.setGridlinesVisible(e.getSource().isChecked()));
        createCheckableItem(viewSubMenu, "Column and row headings", true,
                e -> spreadsheet.setRowColHeadingsVisible(e.getSource().isChecked()));
        createCheckableItem(viewSubMenu, "Top bar", true,
                e -> spreadsheet.setFunctionBarVisible(e.getSource().isChecked()));
        createCheckableItem(viewSubMenu, "Bottom bar", true,
                e -> spreadsheet.setSheetSelectionBarVisible(e.getSource().isChecked()));
        createCheckableItem(viewSubMenu, "Report mode", false,
                e -> spreadsheet.setReportStyle(e.getSource().isChecked()));

        MenuItem formatMenu = menuBar.addItem("Format");
        SubMenu formatSubMenu = formatMenu.getSubMenu();

        createIconItem(formatSubMenu, VaadinIcon.BOLD, "Bold", "Bold",
                e -> changeSelectedCellsFont(font -> font.setBold(!font.getBold())));
        createIconItem(formatSubMenu, VaadinIcon.ITALIC, "Italic", "Italic",
                e -> changeSelectedCellsFont(font -> font.setItalic(!font.getItalic())));

        MenuItem colorMenu = formatSubMenu.addItem("Color");
        SubMenu colorSubMenu = colorMenu.getSubMenu();

        MenuItem textColorMenu = colorSubMenu.addItem("Text");
        textColorMenu.getSubMenu().addItem("Black",
                e -> changeSelectedCellsFont(font -> font.setColor(new XSSFColor(Color.BLACK, null))));
        textColorMenu.getSubMenu().addItem("Blue",
                e -> changeSelectedCellsFont(font -> font.setColor(new XSSFColor(Color.BLUE, null))));
        textColorMenu.getSubMenu().addItem("Red",
                e -> changeSelectedCellsFont(font -> font.setColor(new XSSFColor(Color.RED, null))));
        textColorMenu.getSubMenu().addItem("Green",
                e -> changeSelectedCellsFont(font -> font.setColor(new XSSFColor(Color.GREEN, null))));
        textColorMenu.getSubMenu().addItem("Orange",
                e -> changeSelectedCellsFont(font -> font.setColor(new XSSFColor(Color.ORANGE, null))));

        MenuItem backgroundColorMenu = colorSubMenu.addItem("Background");
        backgroundColorMenu.getSubMenu().addItem("Light gray", e -> changeSelectedCellsStyle(
                cellStyle -> cellStyle.setFillBackgroundColor(new XSSFColor(Color.LIGHT_GRAY, null))));
        backgroundColorMenu.getSubMenu().addItem("White", e -> changeSelectedCellsStyle(
                cellStyle -> cellStyle.setFillBackgroundColor(new XSSFColor(Color.WHITE, null))));
        backgroundColorMenu.getSubMenu().addItem("Cyan", e -> changeSelectedCellsStyle(
                cellStyle -> cellStyle.setFillBackgroundColor(new XSSFColor(Color.CYAN, null))));
        backgroundColorMenu.getSubMenu().addItem("Pink", e -> changeSelectedCellsStyle(
                cellStyle -> cellStyle.setFillBackgroundColor(new XSSFColor(Color.PINK, null))));
        backgroundColorMenu.getSubMenu().addItem("Yellow", e -> changeSelectedCellsStyle(
                cellStyle -> cellStyle.setFillBackgroundColor(new XSSFColor(Color.YELLOW, null))));
        backgroundColorMenu.getSubMenu().addItem("Dark gray", e -> changeSelectedCellsStyle(
                cellStyle -> cellStyle.setFillBackgroundColor(new XSSFColor(Color.DARK_GRAY, null))));

        MenuItem mergeMenu = menuBar.addItem("Merge");
        SubMenu mergeSubMenu = mergeMenu.getSubMenu();

        mergeSubMenu.addItem("Merge selected", e -> mergeSelectedCells(selectedCells.get()));
        mergeSubMenu.addItem("Unmerge selected", e -> unmergeSelectedRegion(selectedCellMergedRegion.get()));

        MenuItem miscMenu = menuBar.addItem("Miscellaneous");
        SubMenu miscSubMenu = miscMenu.getSubMenu();
        miscSubMenu.addItem("Add comment", e -> addComment(selectedCellReference.get()));

        MenuItem freezePanesMenu = miscSubMenu.addItem("Freeze panes");
        SubMenu freezePanesSubMenu = freezePanesMenu.getSubMenu();
        freezePanesSubMenu.addItem("Freeze columns to selected", e -> spreadsheet
                .createFreezePane(spreadsheet.getLastFrozenRow(), spreadsheet.getSelectedCellReference().getCol()));
        freezePanesSubMenu.addItem("Freeze rows to selected", e -> spreadsheet
                .createFreezePane(spreadsheet.getSelectedCellReference().getRow(), spreadsheet.getLastFrozenColumn()));
        freezePanesSubMenu.addItem("Unfreeze all", e -> spreadsheet.removeFreezePane());

        MenuItem tableMenu = miscSubMenu.addItem("Table");
        SubMenu tableSubMenu = tableMenu.getSubMenu();
        tableSubMenu.addItem("Create table", e -> createTable(selectedCells.get()));

        return menuBar;
    }

    private Dialog createUploadDialog() {
        Upload uploadSpreadsheet = new Upload(this);

        Dialog uploadFileDialog = new Dialog();
        uploadFileDialog.setHeaderTitle("Upload a spreadsheet file");
        uploadFileDialog.addOpenedChangeListener(e -> {
            uploadSpreadsheet.clearFileList();
        });
        uploadFileDialog.add(uploadSpreadsheet);

        Button openSpreadsheetButton = new Button("Open spreadsheet", ev -> {
            if (uploadedFile != null) {
                try {
                    if (previousFile == null
                            || !previousFile.getAbsolutePath().equals(uploadedFile.getAbsolutePath())) {
                        spreadsheet.read(uploadedFile);
                        previousFile = uploadedFile;
                        uploadFileDialog.close();
                    } else {
                        Notification.show("Please, select a different file.");
                    }
                } catch (Exception e) {
                    getLogger().warn("ERROR reading file " + uploadedFile, e);
                }
            } else {
                Notification.show("Please, select a file to upload first.");
            }
        });
        openSpreadsheetButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancel", e -> uploadFileDialog.close());

        uploadFileDialog.getFooter().add(cancelButton, openSpreadsheetButton);
        return uploadFileDialog;
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

    private void changeSelectedCellsFont(Consumer<XSSFFont> fontConsumer) {
        changeSelectedCellsStyle(cellStyle -> {
            XSSFFont cellFont = (XSSFFont) cloneFont(cellStyle);
            fontConsumer.accept(cellFont);
            cellStyle.setFont(cellFont);
        });
    }

    private void changeSelectedCellsStyle(Consumer<XSSFCellStyle> cellStyleConsumer) {
        final ArrayList<Cell> cellsToRefresh = new ArrayList<>();
        spreadsheet.getSelectedCellReferences().forEach(cellReference -> {
            Cell cell = getOrCreateCell(cellReference);
            CellStyle cellStyle = cell.getCellStyle();
            XSSFCellStyle newCellStyle = (XSSFCellStyle) spreadsheet.getWorkbook().createCellStyle();
            newCellStyle.cloneStyleFrom(cellStyle);

            cellStyleConsumer.accept(newCellStyle);

            cell.setCellStyle(newCellStyle);

            cellsToRefresh.add(cell);
        });
        spreadsheet.refreshCells(cellsToRefresh);
    }

    private Cell getOrCreateCell(CellReference cellRef) {
        Cell cell = spreadsheet.getCell(cellRef.getRow(), cellRef.getCol());
        if (cell == null) {
            cell = spreadsheet.createCell(cellRef.getRow(), cellRef.getCol(), "");
        }
        return cell;
    }

    private Font cloneFont(CellStyle cellstyle) {
        Font newFont = spreadsheet.getWorkbook().createFont();
        Font originalFont = spreadsheet.getWorkbook().getFontAt(cellstyle.getFontIndex());
        if (originalFont != null) {
            newFont.setBold(originalFont.getBold());
            newFont.setItalic(originalFont.getItalic());
            newFont.setFontHeight(originalFont.getFontHeight());
            newFont.setUnderline(originalFont.getUnderline());
            newFont.setStrikeout(originalFont.getStrikeout());
            // This cast an only be done when using .xlsx files
            XSSFFont originalXFont = (XSSFFont) originalFont;
            XSSFFont newXFont = (XSSFFont) newFont;
            newXFont.setColor(originalXFont.getXSSFColor());
        }
        return newFont;
    }

    private MenuItem createCheckableItem(HasMenuItems menu, String item, boolean checked,
                                         ComponentEventListener<ClickEvent<MenuItem>> clickListener) {
        MenuItem menuItem = menu.addItem(item, clickListener);
        menuItem.setCheckable(true);
        menuItem.setChecked(checked);

        return menuItem;
    }

    private MenuItem createIconItem(HasMenuItems menu, LumoIcon iconName, String label, String ariaLabel,
                                    ComponentEventListener<ClickEvent<MenuItem>> clickListener) {
        Icon icon = new Icon("lumo", iconName.toString().toLowerCase());

        MenuItem item = menu.addItem(icon, clickListener);

        if (ariaLabel != null) {
            item.getElement().setAttribute("aria-label", ariaLabel);
        }

        if (label != null) {
            item.add(new Text(label));
        }

        return item;
    }

    private MenuItem createIconItem(HasMenuItems menu, VaadinIcon iconName, String label, String ariaLabel,
                                    ComponentEventListener<ClickEvent<MenuItem>> clickListener) {
        Icon icon = new Icon(iconName);

        icon.getStyle().set("width", "var(--lumo-icon-size-s)");
        icon.getStyle().set("height", "var(--lumo-icon-size-s)");
        icon.getStyle().set("marginRight", "var(--lumo-space-s)");

        MenuItem item = menu.addItem(icon, clickListener);

        if (ariaLabel != null) {
            item.getElement().setAttribute("aria-label", ariaLabel);
        }

        if (label != null) {
            item.add(new Text(label));
        }

        return item;
    }

    private void mergeSelectedCells(CellRangeAddress selectedCells) {
        if (selectedCells == null) {
            Notification.show("Please select a region of cells to be merged.");
            return;
        }
        spreadsheet.addMergedRegion(selectedCells);
    }

    private void unmergeSelectedRegion(CellRangeAddress selectedCellMergedRegion) {
        if (selectedCellMergedRegion == null) {
            Notification.show("Please select a merged region of cells to be unmerged.");
            return;
        }
        for (int i = 0; i < spreadsheet.getActiveSheet().getNumMergedRegions(); i++) {
            CellRangeAddress mergedRegion = spreadsheet.getActiveSheet().getMergedRegion(i);
            if (selectedCellMergedRegion.getFirstRow() == mergedRegion.getFirstRow()
                    && selectedCellMergedRegion.getFirstColumn() == mergedRegion.getFirstColumn()) {
                spreadsheet.removeMergedRegion(i);
            }
        }
    }

    private void addComment(CellReference cellReference) {
        Cell cell = getOrCreateCell(cellReference);
        createCellComment(spreadsheet, spreadsheet.getActiveSheet(), cell, cellReference);
        spreadsheet.refreshCells(cell);
        spreadsheet.editCellComment(cellReference);
    }

    private void createCellComment(Spreadsheet spreadsheet, Sheet sheet, Cell cell, CellReference cellRef) {
        CreationHelper factory = sheet.getWorkbook().getCreationHelper();
        Drawing<?> drawing = sheet.createDrawingPatriarch();

        ClientAnchor anchor = factory.createClientAnchor();
        anchor.setCol1(cell.getColumnIndex());
        anchor.setCol2(cell.getColumnIndex() + 1);
        anchor.setRow1(cell.getRowIndex());
        anchor.setRow2(cell.getRowIndex() + 3);

        // Create the comment and set the text+author
        Comment comment = drawing.createCellComment(anchor);
        RichTextString str = factory.createRichTextString("");
        comment.setString(str);

        // Fetch author from provider or fall back to default
        String author = null;
        if (spreadsheet.getCommentAuthorProvider() != null) {
            author = spreadsheet.getCommentAuthorProvider().getAuthorForComment(cellRef);
        }
        if (author == null || author.trim().isEmpty()) {
            author = "Spreadsheet User";
        }
        comment.setAuthor(author);

        // Assign the comment to the cell
        cell.setCellComment(comment);
    }

    private void createTable(CellRangeAddress cellAddresses) {
        if (cellAddresses == null) {
            Notification.show("Please select a region of cells to create the table.");
            return;
        }
        SpreadsheetTable table = new SpreadsheetFilterTable(spreadsheet, cellAddresses);
        spreadsheet.registerTable(table);
    }

    private Set<Integer> sieveOfEratosthenes(int n)
    {
        Set<Integer> primeNumbers = new HashSet<>();
        boolean prime[] = new boolean[n + 1];
        for (int i = 0; i <= n; i++)
            prime[i] = true;

        for (int p = 2; p * p <= n; p++) {
            // If prime[p] is not changed, then it is a
            // prime
            if (prime[p] == true) {
                // Update all multiples of p
                for (int i = p * p; i <= n; i += p)
                    prime[i] = false;
            }
        }

        // Print all prime numbers
        for (int i = 2; i <= n; i++) {
            if (prime[i] == true)
                primeNumbers.add(i);
        }

        return primeNumbers;
    }
}
