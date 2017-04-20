/* 
 * Copyright 2012-2017 qifu of copyright Chen Xin Nien
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * -----------------------------------------------------------------------
 * 
 * author: 	Chen Xin Nien
 * contact: chen.xin.nien@gmail.com
 * 
 */
package org.qifu.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.qifu.base.AppContext;
import org.qifu.base.Constants;
import org.qifu.base.SysMessageUtil;
import org.qifu.base.SysMsgConstants;
import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.YesNo;
import org.qifu.model.MenuResultObj;
import org.qifu.po.TbSys;
import org.qifu.po.TbSysMenu;
import org.qifu.po.TbSysProg;
import org.qifu.service.ISysMenuService;
import org.qifu.service.ISysProgService;
import org.qifu.service.ISysService;
import org.qifu.vo.SysMenuVO;
import org.qifu.vo.SysProgVO;
import org.qifu.vo.SysVO;

@SuppressWarnings("unchecked")
public class MenuSupportUtils {
	private static ISysService<SysVO, TbSys, String> sysService;
	private static ISysMenuService<SysMenuVO, TbSysMenu, String> sysMenuService;
	private static ISysProgService<SysProgVO, TbSysProg, String> sysProgService;
	
	static {
		sysService = (ISysService<SysVO, TbSys, String>)AppContext.getBean("core.service.SysService");		
		sysMenuService = (ISysMenuService<SysMenuVO, TbSysMenu, String>)AppContext.getBean("core.service.SysMenuService");
		sysProgService = (ISysProgService<SysProgVO, TbSysProg, String>)AppContext.getBean("core.service.SysProgService");		
	}
	
	public static TbSysProg loadSysProg(String progId) throws ServiceException, Exception {
		TbSysProg tbSysProg = new TbSysProg();
		tbSysProg.setProgId(progId);
		tbSysProg = sysProgService.findByEntityUK(tbSysProg);
		if (null == tbSysProg) {
			throw new ServiceException(SysMessageUtil.get(SysMsgConstants.DATA_ERRORS));
		}
		return tbSysProg;
	}	
	
	public static String getUrl(String basePath, TbSys sys, TbSysProg sysProg) throws Exception {
		String url = "";
		if (StringUtils.isBlank(sysProg.getUrl())) {
			return url;
		}
		if (YesNo.YES.equals(sys.getIsLocal())) {
			url = basePath + "/" + sysProg.getUrl() + ( (sysProg.getUrl().indexOf("?")>0 || sysProg.getUrl().indexOf("&")>0) ? "&" : "?" ) + Constants.QIFU_PAGE_IN_TAB_IFRAME + "=" + YesNo.YES;
		} else {
			String head = "http://";
			if (basePath.startsWith("https")) {
				head = "https://";
			}
			url = head + sys.getHost() + "/" + sys.getContextPath() + "/" + sysProg.getUrl()
					+ ( (sysProg.getUrl().indexOf("?")>0 || sysProg.getUrl().indexOf("&")>0) ? "&" : "?" ) + Constants.QIFU_PAGE_IN_TAB_IFRAME + "=" + YesNo.YES;
		}
		return url;
	}	
	
	public static String getFirstLoadJavascript() throws ServiceException, Exception {		
		return SystemSettingConfigureUtils.getFirstLoadJavascriptValue();
	}	
	
	public static MenuResultObj getMenuData(String basePath) throws ServiceException, Exception {
		Map<String, String> orderParams = new HashMap<String, String>();
		orderParams.put("name", "asc");
		List<TbSys> sysList = sysService.findListByParams(null, null, orderParams);
		if (sysList==null || sysList.size()<1) { // 必需要有 TB_SYS 資料
			throw new ServiceException(SysMessageUtil.get(SysMsgConstants.DATA_ERRORS));
		}
		MenuResultObj resultObj = new MenuResultObj();
		StringBuilder jsSb = new StringBuilder();
		StringBuilder dropdownHtmlSb = new StringBuilder();
		StringBuilder navHtmlSb = new StringBuilder();
		jsSb.append("var _prog = []; ").append("\n");
		for (TbSys sys : sysList) {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("progSystem", sys.getSysId());
			List<TbSysProg> sysProgList = sysProgService.findListByParams(params);
			for (int i=0; sysProgList!=null && i<sysProgList.size(); i++) {
				TbSysProg sysProg = sysProgList.get(i);
				jsSb.append("_prog.push({\"id\" : \"" + sysProg.getProgId() + "\", \"itemType\" : \"" + sysProg.getItemType() + "\", \"name\" : \"" + sysProg.getName() + "\", \"icon\" : \"" + IconUtils.getUrl(basePath, sysProg.getIcon()) + "\", \"url\" : \"" + getUrl(basePath, sys, sysProg) + "\"});").append("\n");
			}			
		}
		
		resultObj.setDropdownHtmlData(dropdownHtmlSb.toString());
		resultObj.setNavItemHtmlData(navHtmlSb.toString());
		resultObj.setJavascriptData(jsSb.toString());
		return resultObj;
	}
	
}
