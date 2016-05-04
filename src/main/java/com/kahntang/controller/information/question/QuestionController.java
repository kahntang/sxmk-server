package com.kahntang.controller.information.question;

import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.kahntang.controller.base.BaseController;
import com.kahntang.entity.Page;
import com.kahntang.service.information.question.QuestionService;
import com.kahntang.util.AppUtil;
import com.kahntang.util.Const;
import com.kahntang.util.DateUtil;
import com.kahntang.util.DelAllFile;
import com.kahntang.util.FileUpload;
import com.kahntang.util.Jurisdiction;
import com.kahntang.util.ObjectExcelView;
import com.kahntang.util.PageData;
import com.kahntang.util.PathUtil;
import com.kahntang.util.Tools;
import com.kahntang.util.Watermark;

 
/**
   * @ClassName: QuestionController
   * @Description: TODO
   * @author Comsys-Administrator
   * @date 2016年4月3日 下午2:22:50
   *
   */
@Controller
@RequestMapping(value="/question")
public class QuestionController extends BaseController {
	
	String menuUrl = "question/list.do"; //菜单地址(权限用)
	@Resource(name="questionService")
	private QuestionService questionService;
	
	 
	/**
	 * 删除
	 */
	@RequestMapping(value="/delete")
	public void delete(PrintWriter out){
		logBefore(logger, "删除qeustion");
		PageData pd = new PageData();
		try{
			if(Jurisdiction.buttonJurisdiction(menuUrl, "del")){
				pd = this.getPageData();
				questionService.delete(pd);
			}
			out.write("success");
			out.close();
		} catch(Exception e){
			logger.error(e.toString(), e);
		}
		
	}
	
	 
	
	/**
	 * 列表
	 */
	@RequestMapping(value="/list")
	public ModelAndView list(Page page){
		logBefore(logger, "列表Question");
		ModelAndView mv = this.getModelAndView();
		PageData pd = new PageData();
		try{
			pd = this.getPageData();
			
			String KEYW = pd.getString("keyword");
			
			if(null != KEYW && !"".equals(KEYW)){
				KEYW = KEYW.trim();
				pd.put("KEYW", KEYW);
			}
			
			page.setPd(pd);
			List<PageData>	varList = questionService.list(page);	 
			mv.setViewName("information/question/question_list");
			mv.addObject("varList", varList);
			mv.addObject("pd", pd);
			mv.addObject(Const.SESSION_QX,this.getHC());	//按钮权限
		} catch(Exception e){
			logger.error(e.toString(), e);
		}
		return mv;
	}
	
	/*
	 * 导出到excel
	 * @return
	 */
	@RequestMapping(value="/excel")
	public ModelAndView exportExcel(){
		logBefore(logger, "导出Question到excel");
		ModelAndView mv = new ModelAndView();
		PageData pd = new PageData();
		pd = this.getPageData();
		try{
			Map<String,Object> dataMap = new HashMap<String,Object>();
			List<String> titles = new ArrayList<String>();
			titles.add("序列");	//1
			titles.add("提问人工号");	//2
			titles.add("提问人账号");	//3
			titles.add("提问人姓名");	//3
			titles.add("创建时间");	//4
			titles.add("问题");	//5
			dataMap.put("titles", titles);
			List<PageData> varOList = questionService.listAll(pd);
			List<PageData> varList = new ArrayList<PageData>();
			for(int i=0;i<varOList.size();i++){
				PageData vpd = new PageData();
				vpd.put("var1", varOList.get(i).getString("QUESTION_ID"));	//6
				vpd.put("var2", varOList.get(i).getString("USER_ID"));	//1
				vpd.put("var3", varOList.get(i).getString("USER_NAME"));	//2
				vpd.put("var4", varOList.get(i).getString("NAME"));	//3
				vpd.put("var5", varOList.get(i).getString("CREATETIME"));	//4
				vpd.put("var6", varOList.get(i).getString("QUESTION_TEXT"));	//5
				varList.add(vpd);
			}
			dataMap.put("varList", varList);
			ObjectExcelView erv = new ObjectExcelView();
			mv = new ModelAndView(erv,dataMap);
		} catch(Exception e){
			logger.error(e.toString(), e);
		}
		return mv;
	}
	 
	/* ===============================权限================================== */
	public Map<String, String> getHC(){
		Subject currentUser = SecurityUtils.getSubject();  //shiro管理的session
		Session session = currentUser.getSession();
		return (Map<String, String>)session.getAttribute(Const.SESSION_QX);
	}
	/* ===============================权限================================== */
	
	@InitBinder
	public void initBinder(WebDataBinder binder){
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		binder.registerCustomEditor(Date.class, new CustomDateEditor(format,true));
	}
}
