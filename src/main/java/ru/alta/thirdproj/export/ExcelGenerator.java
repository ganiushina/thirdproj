package ru.alta.thirdproj.export;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.tomcat.jni.Local;
import ru.alta.thirdproj.entites.EmployerNew;
import ru.alta.thirdproj.entites.UserBonusKPI;
import ru.alta.thirdproj.entites.UserBonusNew;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExcelGenerator {

    private List<UserBonusNew> userBonusNewList;
    private List<EmployerNew> paymentList;
    private List<UserBonusKPI> kpiList;

    boolean bonus;
    boolean kpi;
    boolean payment;

    private List<List<Object>> objectList;
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private XSSFSheet sheetKpi;

    private LocalDate date1;

    private LocalDate date2;

//    public ExcelGenerator(List<UserBonusNew> listRecords, LocalDate date1, LocalDate date2) {
//        this.userBonusNewList = listRecords;
//        workbook = new XSSFWorkbook();
//        this.date1 = date1;
//        this.date2 = date2;
//    }
    public ExcelGenerator(List<List<Object>> listRecords, LocalDate date1, LocalDate date2) {
        this.objectList = listRecords;
        workbook = new XSSFWorkbook();
        this.date1 = date1;
        this.date2 = date2;
    }

    private void writeHeader() throws Exception {
        for (int i = 0; i < objectList.size(); i++) {
            if (((ArrayList) objectList.get(i).get(0)).get(0) instanceof UserBonusKPI) {
                kpi = true;
                kpiList =  ((ArrayList) objectList.get(i).get(0));
                
                sheetKpi = workbook.createSheet("KPI");
                Row row2 = sheetKpi.createRow(0);

                CellStyle style = workbook.createCellStyle();
                XSSFFont font = workbook.createFont();
                font.setBold(true);
                font.setFontHeight(16);
                style.setFont(font);

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd LLLL yyyy");

                createCell(row2, 0,  "Даты", style);
                createCell(row2, 1,  date1.format(formatter) + " - " + date2.format(formatter), style);
                Row rowKPI = sheetKpi.createRow(1);
                createHeaderKPI(style, rowKPI);
                kpi = false;
            } else if (((ArrayList) objectList.get(i).get(0)).get(0) instanceof EmployerNew) {
                payment = true;
                paymentList =((ArrayList) objectList.get(i).get(0));
                sheet = workbook.createSheet("Payment");

            }
            else if (((ArrayList) objectList.get(i).get(0)).get(0) instanceof UserBonusNew) {
                userBonusNewList = ((ArrayList) objectList.get(i).get(0));
                bonus= true;
                sheet = workbook.createSheet("Bonus");

            }
        }

        Row row1 = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(16);
        style.setFont(font);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd LLLL yyyy");

        createCell(row1, 0,  "Даты", style);
        createCell(row1, 1,  date1.format(formatter) + " - " + date2.format(formatter), style);


        Row row = sheet.createRow(1);
        if (payment) {
            createHeaderPayment(style, row);
        }
        if (bonus) {
            createHeaderBonus(style, row);
        }

    }

    private void createHeaderBonus(CellStyle style, Row row) {
        createCell(row, 0, "ФИО", style);
        createCell(row, 1, "Должность", style);
        createCell(row, 2, "Департамент", style);
        createCell(row, 3,  "Всего заработано", style);
        createCell(row, 4, "Всего Бонус", style);
        createCell(row, 5, "Месяц", style);
        createCell(row, 6, "Бонус за кандидата", style);
        createCell(row, 7,  "Заработано за кандидата", style);
        createCell(row, 8,    "Кандидат", style);
        createCell(row, 9, "Компания", style);
        createCell(row, 10, "Процент", style);
        createCell(row, 11,  "Процент за предачу", style);
    }

    private void createHeaderPayment(CellStyle style, Row row) {
        createCell(row, 0, "ФИО", style);
        createCell(row, 1, "Департамент", style);
        createCell(row, 2, "Всего Бонус", style);
        createCell(row, 3, "Процент", style);
        createCell(row, 4, "Бонус за кандидата", style);
        createCell(row, 5,    "Кандидат", style);
        createCell(row, 6, "Компания", style);
        createCell(row, 7, "Номер акта", style);
        createCell(row, 8, "Дата акта", style);
        createCell(row, 9, "Дата выплаты бонуса", style);
        createCell(row, 10, "Дата оплаты клиентом", style);
        createCell(row, 11, "Дата оплаты компанией", style);
    }

    private void createHeaderKPI(CellStyle style, Row row) {
        createCell(row, 0, "ФИО", style);
        createCell(row, 1, "Должность", style);
        createCell(row, 2, "Бонус", style);
        createCell(row, 3, "Бонус за 1-ое место", style);
        createCell(row, 4, "Весь бонус", style);
        createCell(row, 5, "Месяц", style);
    }

    private void createCell(Row row, int columnCount, Object value, CellStyle style) {
        if (kpi){
            sheetKpi.autoSizeColumn(columnCount);
        } else {
            sheet.autoSizeColumn(columnCount);
        }

        Cell cell = row.createCell(columnCount);

        if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        }
        else if (value instanceof Long) {
            cell.setCellValue((Long) value);}
        else if (value instanceof Double) {
            cell.setCellValue((Double) value);
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else if  (value instanceof String){
            cell.setCellValue((String) value);
        } else if  (value instanceof ArrayList) {
            style.setWrapText(true);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < ((ArrayList<?>) value).size(); i++) {
                if (((ArrayList<?>) value).get(i) instanceof Integer) {
                    sb.append(String.valueOf((((ArrayList<?>) value).get(i))));
                }
                else if (((ArrayList<?>) value).get(i) instanceof Double) {
                    sb.append(Double.toString((double) ((ArrayList<?>) value).get(i)));
                }
                else {
                    sb.append((String) ((ArrayList<?>) value).get(i));
                }
                sb.append("\n");
            }
            cell.setCellValue(sb.toString());
        }
        style.setVerticalAlignment(VerticalAlignment.TOP);
        cell.setCellStyle(style);

    }

    private void write() {
        int rowCount = 2;
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(14);
        style.setFont(font);

        for (int i = 0; i < objectList.size(); i++) {
            if (((ArrayList) objectList.get(i).get(0)).get(0) instanceof UserBonusKPI) {
                kpi = true;
                createCellKPI(rowCount, style, kpiList);
                kpi = false;
            } else if (((ArrayList) objectList.get(i).get(0)).get(0) instanceof EmployerNew) {
                createCellPayment(rowCount, style, paymentList);

            }
            else if (((ArrayList) objectList.get(i).get(0)).get(0) instanceof UserBonusNew) {
                createCellBonus(rowCount, style, userBonusNewList);
            }
        }

    }

    private void createCellBonus(int rowCount, CellStyle style, List<UserBonusNew> userBonusList) {
        for (UserBonusNew record : userBonusList) {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            if (record.getMoneyAll() != null ) {
                createCell(row, columnCount++, record.getFio(), style);
                createCell(row, columnCount++, record.getPosition(), style);
                createCell(row, columnCount++, record.getDepartment(), style);
                createCell(row, columnCount++, record.getSumTotal(), style);
                createCell(row, columnCount++, record.getMoneyAll(), style);
                createCell(row, columnCount++, record.getMonthName(), style);
                createCell(row, columnCount++, record.getMoneyByCandidate(), style);
                createCell(row, columnCount++, record.getSumUser(), style);
                createCell(row, columnCount++, record.getCandidateName(), style);
                createCell(row, columnCount++, record.getCompanyName(), style);
                createCell(row, columnCount++, record.getPercent(), style);
                createCell(row, columnCount++, record.getExtraBonusAct(), style);
            }
        }
    }

    private void createCellPayment(int rowCount, CellStyle style, List<EmployerNew> payList) {
        for (EmployerNew record : payList) {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            ArrayList<Double> bonus = new ArrayList<>();
            ArrayList<String> candidate = new ArrayList<>();
            ArrayList<String> companies = new ArrayList<>();
            ArrayList<String> num = new ArrayList<>();
            ArrayList<String> date = new ArrayList<>();
            ArrayList<String> dateForPay = new ArrayList<>();
            ArrayList<String> datePayment = new ArrayList<>();
            ArrayList<String> paymentRealDate = new ArrayList<>();

            createCell(row, columnCount++, record.getManFIO(), style);
            createCell(row, columnCount++, record.getUserDepartment(), style);
            createCell(row, columnCount++, record.getAllBonus(), style);
            createCell(row, columnCount++, record.getPercent(), style);

            for (int i = 0; i < record.getActList().size(); i++) {
                bonus.add(record.getActList().get(i).getBonus());
                candidate.add(record.getActList().get(i).getCandidate());
                companies.add(record.getActList().get(i).getCompanies());
                num.add(record.getActList().get(i).getNum());
                date.add(record.getActList().get(i).getDate());
                dateForPay.add(record.getActList().get(i).getDateForPay());
                datePayment.add(record.getActList().get(i).getDatePayment());
                paymentRealDate.add(record.getActList().get(i).getPaymentRealDate());
            }

            createCell(row, columnCount++, bonus, style);
            createCell(row, columnCount++, candidate, style);
            createCell(row, columnCount++, companies, style);
            createCell(row, columnCount++, num, style);
            createCell(row, columnCount++, date, style);
            createCell(row, columnCount++, dateForPay, style);
            createCell(row, columnCount++, datePayment, style);
            createCell(row, columnCount++, paymentRealDate, style);
        }
    }

    private void createCellKPI(int rowCount, CellStyle style, List<UserBonusKPI> kpiLst) {
        for (UserBonusKPI record : kpiLst) {
            Row row = sheetKpi.createRow(rowCount++);
            int columnCount = 0;

            createCell(row, columnCount++, record.getFio(), style);
            createCell(row, columnCount++, record.getPosition(), style);
            createCell(row, columnCount++, record.getBonus(), style);
            createCell(row, columnCount++, record.getBonusBest(), style);
            createCell(row, columnCount++, record.getBonusAll(), style);
            createCell(row, columnCount++, record.getMonth(), style);
        }
    }


    public void generate(HttpServletResponse response) throws Exception {
        writeHeader();
        write();
        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();

    }
}
