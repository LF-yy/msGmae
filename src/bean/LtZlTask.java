package bean;

import java.util.Arrays;
import java.util.List;

public class LtZlTask {
    // 战令任务表
    // 字段说明;//
    private long id;//主键
    private int time_code;//时间编码，1=每日任务，2=周任务，3=月任务
    private int pt_zl_jf;//普通战令积分
    private int gj_zl_jf;//高级战令积分
    private int task_type;//任务类型 1=累计通关任务,2=击杀任务,3=累计击杀任务,4=收集任务,5=累计收集任务
    private String code_list;//任务需求编码 多个编码以","隔开
    private List<String> codeList;//任务需求编码 多个编码以","隔开
    private String code_number;//任务需求数量集 多个数量以","隔开
    private List<String> codeNumber;//任务需求数量集 多个数量以","隔开
    private String remark;//任务描述

    public LtZlTask(long id, int time_code, int pt_zl_jf, int gj_zl_jf, int task_type, String code_list,  String code_number, String remark) {
        this.id = id;
        this.time_code = time_code;
        this.pt_zl_jf = pt_zl_jf;
        this.gj_zl_jf = gj_zl_jf;
        this.task_type = task_type;
        this.code_list = code_list;
        this.codeList = Arrays.asList(code_list.split(","));
        this.code_number = code_number;
        this.codeNumber = Arrays.asList(code_number.split(","));
        this.remark = remark;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getTime_code() {
        return time_code;
    }

    public void setTime_code(int time_code) {
        this.time_code = time_code;
    }

    public int getPt_zl_jf() {
        return pt_zl_jf;
    }

    public void setPt_zl_jf(int pt_zl_jf) {
        this.pt_zl_jf = pt_zl_jf;
    }

    public int getGj_zl_jf() {
        return gj_zl_jf;
    }

    public void setGj_zl_jf(int gj_zl_jf) {
        this.gj_zl_jf = gj_zl_jf;
    }

    public int getTask_type() {
        return task_type;
    }

    public void setTask_type(int task_type) {
        this.task_type = task_type;
    }

    public String getCode_list() {
        return code_list;
    }

    public void setCode_list(String code_list) {
        this.code_list = code_list;
    }

    public List<String> getCodeList() {
        return codeList;
    }

    public void setCodeList(List<String> codeList) {
        this.codeList = codeList;
    }

    public String getCode_number() {
        return code_number;
    }

    public void setCode_number(String code_number) {
        this.code_number = code_number;
    }

    public List<String> getCodeNumber() {
        return codeNumber;
    }

    public void setCodeNumber(List<String> codeNumber) {
        this.codeNumber = codeNumber;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
