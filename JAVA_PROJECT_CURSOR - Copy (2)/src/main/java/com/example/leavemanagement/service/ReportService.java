package com.example.leavemanagement.service;

import com.example.leavemanagement.model.LeaveRequest;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class ReportService {

    public byte[] exportExcel(List<LeaveRequest> requests) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Leaves");
            int r = 0;
            Row header = sheet.createRow(r++);
            header.createCell(0).setCellValue("Employee");
            header.createCell(1).setCellValue("Type");
            header.createCell(2).setCellValue("Status");
            header.createCell(3).setCellValue("Start");
            header.createCell(4).setCellValue("End");
            header.createCell(5).setCellValue("Days");

            for (LeaveRequest lr : requests) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(lr.getEmployee().getFullName());
                row.createCell(1).setCellValue(lr.getLeaveType().name());
                row.createCell(2).setCellValue(lr.getStatus().name());
                row.createCell(3).setCellValue(lr.getStartDate().toString());
                row.createCell(4).setCellValue(lr.getEndDate().toString());
                row.createCell(5).setCellValue(lr.getTotalDays());
            }
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Excel", e);
        }
    }

    public byte[] exportXml(ListWrapper wrapper) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            JAXBContext jaxbContext = JAXBContext.newInstance(ListWrapper.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(wrapper, out);
            return out.toByteArray();
        } catch (JAXBException e) {
            throw new RuntimeException("Failed to generate XML", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @jakarta.xml.bind.annotation.XmlRootElement(name = "leaves")
    @jakarta.xml.bind.annotation.XmlAccessorType(jakarta.xml.bind.annotation.XmlAccessType.FIELD)
    public static class ListWrapper {
        @jakarta.xml.bind.annotation.XmlElement(name = "leave")
        public List<LeaveEntry> items;
    }

    @jakarta.xml.bind.annotation.XmlAccessorType(jakarta.xml.bind.annotation.XmlAccessType.FIELD)
    public static class LeaveEntry {
        public String employee;
        public String type;
        public String status;
        public String startDate;
        public String endDate;
        public int days;
    }
}



