package com.alivc.base;

import java.io.Serializable;
import java.util.List;

/** 
 * ClassName: PageUtil <br/> 
 * Function: TODO 分页模型. <br/> 
 * Reason:   TODO 用于给功能模块提供分页的数据模型. <br/> 
 * Date:     2018年11月10日  <br/> 
 * @author   tz 
 * @version   v0.0.1
 * @since    JDK 1.8 
 * @see       
 */
public class PageUtil<T> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5400606092084259125L;

	/**
	 * 页码
	 */
	private int page = 1;

	/**
	 * 每页记录条数
	 */
	private int pageSize = 10;

	/**
	 * 总记录数
	 */
	private Long total;

	private Integer num;

	/**
	 * 上一页
	 */
	private String previous;

	/**
	 * 下一页
	 */
	private String next;

	/**
	 * 数据集合
	 */
	private List<T> list;

	public Integer getNum() {
		return num;
	}

	public void setNum(Integer num) {
		this.num = num;
	}

	public int getPage() {
		if (page <= 0) {
			page = 1;
		}
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getPageSize() {
		if (pageSize <= 0) {
			pageSize = 10;
		}
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public Long getTotal() {
		return total;
	}

	public void setTotal(Long total) {
		this.total = total;
	}

	public String getPrevious() {
		return previous;
	}

	public void setPrevious(String previous) {
		this.previous = previous;
	}

	public String getNext() {
		return next;
	}

	public void setNext(String next) {
		this.next = next;
	}

	public List<T> getList() {
		return list;
	}

	public void setList(List<T> list) {
		this.list = list;
	}

	public long getStart() {
		return (getPage() - 1) * getPageSize();
	}
}
