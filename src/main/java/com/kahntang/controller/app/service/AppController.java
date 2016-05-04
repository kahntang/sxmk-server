/**
  * @Title: AppController.java
  * @Package com.kahntang.controller.app.service
  * @Description: TODO
  * Copyright: Copyright (c) 2016 
  * Company:个人
  * 
  * @author Comsys-Administrator
  * @date 2016年4月2日 下午5:28:05
  * @version V1.0
  */
package com.kahntang.controller.app.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.kahntang.controller.base.BaseController;
import com.kahntang.entity.Page;
import com.kahntang.entity.system.Role;
import com.kahntang.entity.system.User;
import com.kahntang.service.information.pictures.OutputService;
import com.kahntang.service.information.pictures.PicturesService;
import com.kahntang.service.information.question.QuestionService;
import com.kahntang.service.system.dictionaries.DictionariesService;
import com.kahntang.service.system.user.UserService;
import com.kahntang.util.AppUtil;
import com.kahntang.util.Const;
import com.kahntang.util.DateUtil;
import com.kahntang.util.PageData;
import com.kahntang.util.Tools;

import org.codehaus.jackson.type.JavaType;
/**
   * @ClassName: AppController
   * @Description: TODO
   * @author Comsys-Administrator
   * @date 2016年4月2日 下午5:28:05
   *
   */
//@RestController
@Controller
@RequestMapping(value="/app")
public class AppController extends BaseController {
	@Resource(name="userService")
	private UserService userService;
	@Resource(name="questionService")
	private QuestionService questionService;
	@Resource(name="picturesService")
	private PicturesService picturesService;
	@Resource(name="outputService")
	private OutputService outputService;
	@Resource(name="dictionariesService")
	private DictionariesService dictionariesService;
	
	@RequestMapping(value="/checkUserInfo")
	@ResponseBody
	public Object checkUserInfo(){
		logBefore(logger, "验证客户端用户名密码并返回token");
		Map<String,Object> map = new HashMap<String,Object>(); 
		PageData pd = new PageData();
		pd = this.getPageData();
		String errorCode = "00";
		String errorMsg = "sucess";

		try{
			if(Tools.checkKey("USERNAME", pd.getString("FKEY"))){	//检验请求key值是否合法
				String username = pd.getString("USERNAME");
				String password = pd.getString("PASSWORD");
				if(username==null || username.equals("") || password==null || password.equals("")){
					errorCode = "99";
					errorMsg = "用户名或密码为空";
				}
				else{
					String passwd = new SimpleHash("SHA-1", username, password).toString();	//密码加密
					pd.put("PASSWORD", passwd);
					pd = userService.getUserByNameAndPwd(pd);
					if(pd != null){
						String tokenId = this.get32UUID();
						pd.put("LAST_LOGIN",DateUtil.getTime().toString());
						pd.put("TOKEN",tokenId);
						pd.put("TYPE", "MOBILE");
						pd.put("SESSIONID", tokenId);
						userService.insertLogLogin(pd);
						
						userService.updateLastLogin(pd);
						PageData key = userService.getAppKeyId();
						String appKey = key.getString("APP_KEY");
						String appId  = key.getString("APP_ID");
						User user = new User();
						user.setUSER_ID(pd.getString("USER_ID"));
						user.setUSERNAME(pd.getString("USERNAME"));
						user.setPASSWORD(pd.getString("PASSWORD"));
						user.setNAME(pd.getString("NAME"));
						user.setRIGHTS(pd.getString("RIGHTS"));
						user.setROLE_ID(pd.getString("ROLE_ID"));
						user.setLAST_LOGIN(pd.getString("LAST_LOGIN"));
						user.setIP(pd.getString("IP"));
						user.setSTATUS(pd.getString("STATUS"));
						//shiro加入身份验证
						Subject subject = SecurityUtils.getSubject(); 
					    UsernamePasswordToken token = new UsernamePasswordToken(username, password); 
					    try { 
					        subject.login(token); 
					        map.putAll(pd);
					        map.put("appkey", appKey);
					        map.put("appid", appId);
					        map.put("token", tokenId);
					    } catch (AuthenticationException e) { 
					    	errorCode = "98";
							errorMsg = "身份验证失败";
					    }
					    
					}else{
						errorCode = "97";
						errorMsg = "用户名或密码有误";				//用户名或密码有误
					}
				}
			}else{
				errorCode = "05";
				errorMsg = "签名有误";				 
			}
		}catch (Exception e){
			errorCode="-1";
			errorMsg = "异常错误"; 
			logger.error(e.toString(), e);
		}finally{
			map.put("errorCode", errorCode);
			map.put("errorMsg", errorMsg);
			logAfter(logger);
		}
		
		return AppUtil.returnObject(new PageData(), map);
	}
	
	
	@RequestMapping(value="/register")
	@ResponseBody
	public Object register(){
		logBefore(logger, "新用户注册");
		Map<String,Object> map = new HashMap<String,Object>(); 
		PageData pd = new PageData();
		pd = this.getPageData();
		String errorCode = "00";
		String errorMsg = "sucess";

		try{
			if(Tools.checkKey("USERNAME", pd.getString("FKEY"))){	//检验请求key值是否合法
				String username = pd.getString("USERNAME");
				String password = pd.getString("PASSWORD");
				if(username==null || username.equals("") 
						|| password==null || password.equals("") 
						){
					errorCode = "96";
					errorMsg = "信息不合法";
				}
				else{
					String passwd = new SimpleHash("SHA-1", username, password).toString();	//密码加密
					pd.put("PASSWORD", passwd);
					pd = userService.findByUId(pd);
					if(pd == null){
						pd = new PageData();
						pd.put("PASSWORD", passwd);
						pd.put("PHONE",username);
						pd.put("NAME", username);
						pd.put("USERNAME", username);
						pd.put("ROLE_ID", "d712774958954e608ffa1ad53364eaba");
						String tokenId = this.get32UUID();
						pd.put("USER_ID", tokenId);	//ID
						pd.put("RIGHTS", "");					//权限
						pd.put("LAST_LOGIN", "");				//最后登录时间
						pd.put("IP", "");						//IP
						pd.put("STATUS", "0");					//状态
						pd.put("SKIN", "default");				//默认皮肤
						pd.put("TOKEN", "");
						userService.saveU(pd);
					}else{
						errorCode = "01";
						errorMsg = "手机号码已注册";				//用户名或密码有误
					}
				}
			}else{
				errorCode = "05";
				errorMsg = "签名有误";				 
			}
		}catch (Exception e){
			errorCode="-1";
			errorMsg = "异常错误"; 
			logger.error(e.toString(), e);
		}finally{
			map.put("errorCode", errorCode);
			map.put("errorMsg", errorMsg);
			logAfter(logger);
		}
		
		return AppUtil.returnObject(new PageData(), map);
	}
	
	
	@RequestMapping(value="/updateUserPwd")
	@ResponseBody
	public Object updateUserPwd(){
		logBefore(logger, "修改用户密码");
		Map<String,Object> map = new HashMap<String,Object>(); 
		PageData pd = new PageData();
		pd = this.getPageData();
		String errorCode = "00";
		String errorMsg = "sucess";

		try{
			if(Tools.checkKey("USERNAME", pd.getString("FKEY"))){	//检验请求key值是否合法
				String username = pd.getString("USERNAME");
				String password = pd.getString("PASSWORD");
				String	tokenId 	= pd.getString("TOKEN");
				if(username==null || username.equals("") || password==null || password.equals("") 
						|| tokenId==null || tokenId.equals("")){
					errorCode = "96";
					errorMsg = "信息不合法";
				}
				else{
					String passwd = new SimpleHash("SHA-1", username, password).toString();	//密码加密
					pd.put("PASSWORD", passwd);
					pd = userService.checkUserToken(pd);
					if(pd != null){
						pd.put("PASSWORD", passwd);
						tokenId = this.get32UUID();
						pd.put("TOKEN", tokenId);
						userService.saveP(pd);
						map.put("TOKEN", tokenId);
					}else{
						errorCode = "95";
						errorMsg = "token不正确";				//用户名或密码有误
					}
				}
			}else{
				errorCode = "05";
				errorMsg = "签名有误";				 
			}
		}catch (Exception e){
			errorCode="-1";
			errorMsg = "异常错误"; 
			logger.error(e.toString(), e);
		}finally{
			map.put("errorCode", errorCode);
			map.put("errorMsg", errorMsg);
			logAfter(logger);
		}
		
		return AppUtil.returnObject(new PageData(), map);
	}
	
	@RequestMapping(value="/updateUserInfo")
	@ResponseBody
	public Object updateUserInfo(){
		logBefore(logger, "修改用户信息");
		Map<String,Object> map = new HashMap<String,Object>(); 
		PageData pd = new PageData();
		pd = this.getPageData();
		String errorCode = "00";
		String errorMsg = "sucess";

		try{
			if(Tools.checkKey("USERNAME", pd.getString("FKEY"))){	//检验请求key值是否合法
				String  username 	= pd.getString("USERNAME");
				String	tokenId 	= pd.getString("TOKEN");
				String  phone		= pd.getString("PHONE");
				String  email		= pd.getString("EMAIL");
				String  sex			= pd.getString("SEX");
				String  name		= pd.getString("NAME");
				String  bz			= pd.getString("BZ");
				String  address		= pd.getString("ADDRESS");
				if(username==null || username.equals("")   
						|| tokenId==null || tokenId.equals("") 
						|| name==null || name.equals("")
						|| phone==null || phone.length()!=11){
					errorCode = "96";
					errorMsg = "信息不合法";
				}
				else{
					pd = userService.checkUserToken(pd);
					if(pd != null){
						if(phone!=null && phone.length()==11){
							pd = userService.findByPhone(pd);
							if(pd!=null){
								errorCode = "2";
								errorMsg = "手机号码已存在";
							}
							else{
								pd = new PageData();
								pd.put("USERNAME", username);
								pd.put("TOKEN", tokenId);
								pd = userService.checkUserToken(pd);
								pd.put("NAME", name);
								pd.put("SEX", sex);
								pd.put("BZ", bz);
								pd.put("EMAIL", email);
								pd.put("PHONE", phone);
								pd.put("ADDRESS", address);
								userService.editU(pd);
								map.putAll(pd);
							}
						}
						else{
							pd.put("NAME", name);
							pd.put("SEX", sex);
							pd.put("BZ", bz);
							pd.put("EMAIL", email);
							pd.put("PHONE", phone);
							pd.put("ADDRESS", address);
							userService.editU(pd);
							map.putAll(pd);
						}
						
					}else{
						errorCode = "95";
						errorMsg = "token不正确";				//用户名或密码有误
					}
				}
			}else{
				errorCode = "05";
				errorMsg = "签名有误";				 
			}
		}catch (Exception e){
			errorCode="-1";
			errorMsg = "异常错误"; 
			logger.error(e.toString(), e);
		}finally{
			map.put("errorCode", errorCode);
			map.put("errorMsg", errorMsg);
			logAfter(logger);
		}
		
		return AppUtil.returnObject(new PageData(), map);
	}
	
	
	/**获取通讯录
	 * @param page
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/listUsers")
	@ResponseBody
	public Object listUsers()throws Exception{
		logBefore(logger, "获取通讯录");
		Map<String,Object> map = new HashMap<String,Object>(); 
		PageData pd = new PageData();
		Page page = new Page();
		pd = this.getPageData();
		String errorCode = "00";
		String errorMsg = "sucess";

		try{
			if(Tools.checkKey("USERNAME", pd.getString("FKEY"))){	//检验请求key值是否合法
				String username = pd.getString("USERNAME");
				String	tokenId 	= pd.getString("TOKEN");
				if(username==null || username.equals("")  
						|| tokenId==null || tokenId.equals("")){
					errorCode = "96";
					errorMsg = "信息不合法";
				}
				else{
					String  pageNo 		= pd.getString("PAGENO");
					String	pageSize 	= pd.getString("PAGESIZE");
					pd = userService.checkUserToken(pd);
					if(pd != null){
						page.setCurrentPage(Integer.parseInt(pageNo));
						page.setShowCount(Integer.parseInt(pageSize));
						
						page.setPd(new PageData());
						List<PageData>	userList = userService.listPdPageUser(page);			//列出用户列表
						map.put("list", userList);
					}else{
						errorCode = "95";
						errorMsg = "token不正确";				//用户名或密码有误
					}
				}
			}else{
				errorCode = "05";
				errorMsg = "签名有误";				 
			}
		}catch (Exception e){
			errorCode="-1";
			errorMsg = "异常错误"; 
			logger.error(e.toString(), e);
		}finally{
			map.put("errorCode", errorCode);
			map.put("errorMsg", errorMsg);
			logAfter(logger);
		}
		
		return AppUtil.returnObject(new PageData(), map);
		 
	}

	
	@RequestMapping(value="/listPictures")
	@ResponseBody
	public Object listPictures(){
		logBefore(logger, "获取首页图片");
		Map<String,Object> map = new HashMap<String,Object>(); 
		PageData pd = new PageData();
		
		pd = this.getPageData();
		String errorCode = "00";
		String errorMsg = "sucess";

		try{
			if(Tools.checkKey("USERNAME", pd.getString("FKEY"))){	//检验请求key值是否合法
				String username = pd.getString("USERNAME");
				String	tokenId 	= pd.getString("TOKEN");
				if(username==null || username.equals("")  
						|| tokenId==null || tokenId.equals("")){
					errorCode = "96";
					errorMsg = "信息不合法";
				}
				else{
					pd = userService.checkUserToken(pd);
					if(pd != null){
						
						pd.put("BIANMA", "WEBROOT");
						pd = dictionariesService.findBmCount(pd);
						pd = dictionariesService.findById(pd);
						String root = pd.getString("NAME");
						List<PageData>	varOList = picturesService.listAll(pd);
						List<PageData> varList = new ArrayList<PageData>();
						for(int i=0;i<varOList.size();i++){
							PageData vpd = new PageData();
							vpd.put("PATH", root+varOList.get(i).getString("PATH"));	//1
							varList.add(vpd);
						} 
						map.put("LIST", varList);
					}else{
						errorCode = "95";
						errorMsg = "token不正确";				//用户名或密码有误
					}
				}
			}else{
				errorCode = "05";
				errorMsg = "签名有误";				 
			}
		}catch (Exception e){
			errorCode="-1";
			errorMsg = "异常错误"; 
			logger.error(e.toString(), e);
		}finally{
			map.put("errorCode", errorCode);
			map.put("errorMsg", errorMsg);
			logAfter(logger);
		}
		
		return AppUtil.returnObject(new PageData(), map);
	}
	
	
	@RequestMapping(value="/listOutput")
	@ResponseBody
	public Object listOutput(){
		logBefore(logger, "获取布局");
		Map<String,Object> map = new HashMap<String,Object>(); 
		PageData pd = new PageData();
		
		pd = this.getPageData();
		String errorCode = "00";
		String errorMsg = "sucess";

		try{
			if(Tools.checkKey("USERNAME", pd.getString("FKEY"))){	//检验请求key值是否合法
				String username = pd.getString("USERNAME");
				String	tokenId 	= pd.getString("TOKEN");
				if(username==null || username.equals("")  
						|| tokenId==null || tokenId.equals("")){
					errorCode = "96";
					errorMsg = "信息不合法";
				}
				else{
					pd = userService.checkUserToken(pd);
					if(pd != null){
						
						pd.put("BIANMA", "WEBROOT");
						pd = dictionariesService.findBmCount(pd);
						pd = dictionariesService.findById(pd);
						String root = pd.getString("NAME");
						List<PageData>	varOList = outputService.listAll(pd);
						List<PageData> varList = new ArrayList<PageData>();
						for(int i=0;i<varOList.size();i++){
							PageData vpd = new PageData();
							vpd.put("NAME", varOList.get(i).getString("TITLE"));	//1
							vpd.put("PATH", root+varOList.get(i).getString("PATH"));	//1
							vpd.put("URL", varOList.get(i).getString("MASTER_ID"));	//1
							varList.add(vpd);
						} 
						map.put("LIST", varList);
					}else{
						errorCode = "95";
						errorMsg = "token不正确";				//用户名或密码有误
					}
				}
			}else{
				errorCode = "05";
				errorMsg = "签名有误";				 
			}
		}catch (Exception e){
			errorCode="-1";
			errorMsg = "异常错误"; 
			logger.error(e.toString(), e);
		}finally{
			map.put("errorCode", errorCode);
			map.put("errorMsg", errorMsg);
			logAfter(logger);
		}
		
		return AppUtil.returnObject(new PageData(), map);
	}
	
	
	@RequestMapping(value="/logout")
	@ResponseBody
	public Object logout(){
		logBefore(logger, "退出登录");
		Map<String,Object> map = new HashMap<String,Object>(); 
		PageData pd = new PageData();
		pd = this.getPageData();
		String errorCode = "00";
		String errorMsg = "sucess";

		try{
			if(Tools.checkKey("USERNAME", pd.getString("FKEY"))){	//检验请求key值是否合法
				String username = pd.getString("USERNAME");
				String	tokenId 	= pd.getString("TOKEN");
				if(username==null || username.equals("")  
						|| tokenId==null || tokenId.equals("")){
					errorCode = "96";
					errorMsg = "信息不合法";
				}
				else{
					pd = userService.checkUserToken(pd);
					if(pd != null){
						userService.updateLogLogin(pd);
						pd.put("TOKEN", "");
						userService.saveP(pd);
					}else{
						errorCode = "95";
						errorMsg = "token不正确";				//用户名或密码有误
					}
				}
			}else{
				errorCode = "05";
				errorMsg = "签名有误";				 
			}
		}catch (Exception e){
			errorCode="-1";
			errorMsg = "异常错误"; 
			logger.error(e.toString(), e);
		}finally{
			map.put("errorCode", errorCode);
			map.put("errorMsg", errorMsg);
			logAfter(logger);
		}
		
		return AppUtil.returnObject(new PageData(), map);
	}
	
	@RequestMapping(value="/updateQuestion")
	@ResponseBody
	public Object updateQuestion(){
		logBefore(logger, "问题反馈");
		Map<String,Object> map = new HashMap<String,Object>(); 
		PageData pd = new PageData();
		pd = this.getPageData();
		String errorCode = "00";
		String errorMsg = "sucess";

		try{
			if(Tools.checkKey("USERNAME", pd.getString("FKEY"))){	//检验请求key值是否合法
				String username = pd.getString("USERNAME");
				String qdata = pd.getString("QDATA");
				String tokenId 	= pd.getString("TOKEN");
				if(username==null || username.equals("") || qdata==null || qdata.equals("") 
						|| tokenId==null || tokenId.equals("")){
					errorCode = "96";
					errorMsg = "信息不合法";
				}
				else{
					pd = userService.checkUserToken(pd);
					if(pd != null){
						pd.put("QUESTIONID", this.get32UUID());
						pd.put("QDATA", qdata);
						questionService.save(pd);
					}else{
						errorCode = "95";
						errorMsg = "token不正确";				//用户名或密码有误
					}
				}
			}else{
				errorCode = "05";
				errorMsg = "签名有误";				 
			}
		}catch (Exception e){
			errorCode="-1";
			errorMsg = "异常错误"; 
			logger.error(e.toString(), e);
		}finally{
			map.put("errorCode", errorCode);
			map.put("errorMsg", errorMsg);
			logAfter(logger);
		}
		
		return AppUtil.returnObject(new PageData(), map);
	}
	
	
	@RequestMapping("say/{name}")
    public User say(@PathVariable String name) {

		User message = new User();
        message.setNAME(name);
        message.setUSERNAME("hello," + name);

        return message;
    }

}
