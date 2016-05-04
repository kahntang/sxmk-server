package com.kahntang.service.information.question;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.kahntang.dao.DaoSupport;
import com.kahntang.entity.Page;
import com.kahntang.util.PageData;


@Service("questionService")
public class QuestionService {

	@Resource(name = "daoSupport")
	private DaoSupport dao;
	
	/*
	* 新增
	*/
	public void save(PageData pd)throws Exception{
		dao.save("QuestionMapper.save", pd);
	}
	
	/*
	* 删除
	*/
	public void delete(PageData pd)throws Exception{
		dao.delete("QuestionMapper.delete", pd);
	}
	
	
	/*
	*列表
	*/
	public List<PageData> list(Page page)throws Exception{
		return (List<PageData>)dao.findForList("QuestionMapper.datalistPage", page);
	}
	
	/*
	*列表(全部)
	*/
	public List<PageData> listAll(PageData pd)throws Exception{
		return (List<PageData>)dao.findForList("QuestionMapper.listAll", pd);
	}
	
	/*
	* 通过id获取数据
	*/
	public PageData findById(PageData pd)throws Exception{
		return (PageData)dao.findForObject("QuestionMapper.findById", pd);
	}
	
	
}

