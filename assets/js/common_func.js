/*      PPK Common JS Functions           */
/*         PPkPub.org  20201218           */  
/*    Released under the MIT License.     */

//处理startsWith函数不能在任何浏览器兼容的问题
if (typeof String.prototype.startsWith != 'function') {
  String.prototype.startsWith = function (prefix){
    return this.slice(0, prefix.length) === prefix;
  };
}

//缩短显示长地址
function shortCoinAddress(str_address){
    if(!str_address)
        return "unknown";
    
    if(str_address.length<=13){
        return str_address;
    }
    
    return str_address.substring(0,6)+'...'+str_address.substring(str_address.length-4);
}

function changeEthToWei( eth_value ){
    return ""+parseInt(eth_value * 100000000)+"0000000000";
}
  
function changeWeiToEth( wei_value ){
    if(wei_value==0 || wei_value=='0')
        return 0;
        
    wei_str = ""+wei_value;
    //console.log("wei_value="+wei_value+" , wei_str="+wei_str);
    return (0+wei_str.substring(0,wei_str.length-10)) / 100000000;
}

function getERC20TokenContract(contract_address){
    var contract_abi =  [{"anonymous": false,"inputs": [{"indexed": true,"internalType": "address","name": "owner","type": "address"},{"indexed": true,"internalType": "address","name": "spender","type": "address"},{"indexed": false,"internalType": "uint256","name": "value","type": "uint256"}],"name": "Approval","type": "event"},{"anonymous": false,"inputs": [{"indexed": true,"internalType": "address","name": "from","type": "address"},{"indexed": true,"internalType": "address","name": "to","type": "address"},{"indexed": false,"internalType": "uint256","name": "value","type": "uint256"}],"name": "Transfer","type": "event"},{"inputs": [{"internalType": "address","name": "owner","type": "address"},{"internalType": "address","name": "spender","type": "address"}],"name": "allowance","outputs": [{"internalType": "uint256","name": "","type": "uint256"}],"stateMutability": "view","type": "function"},{"inputs": [{"internalType": "address","name": "spender","type": "address"},{"internalType": "uint256","name": "amount","type": "uint256"}],"name": "approve","outputs": [{"internalType": "bool","name": "","type": "bool"}],"stateMutability": "nonpayable","type": "function"},{"inputs": [{"internalType": "address","name": "account","type": "address"}],"name": "balanceOf","outputs": [{"internalType": "uint256","name": "","type": "uint256"}],"stateMutability": "view","type": "function"},{"inputs": [],"name": "totalSupply","outputs": [{"internalType": "uint256","name": "","type": "uint256"}],"stateMutability": "view","type": "function"},{"inputs": [{"internalType": "address","name": "recipient","type": "address"},{"internalType": "uint256","name": "amount","type": "uint256"}],"name": "transfer","outputs": [{"internalType": "bool","name": "","type": "bool"}],"stateMutability": "nonpayable","type": "function"},{"inputs": [{"internalType": "address","name": "sender","type": "address"},{"internalType": "address","name": "recipient","type": "address"},{"internalType": "uint256","name": "amount","type": "uint256"}],"name": "transferFrom","outputs": [{"internalType": "bool","name": "","type": "bool"}],"stateMutability": "nonpayable","type": "function"}];

    
    return new window.web3.eth.Contract(contract_abi, contract_address);
}

function finishedButton(str_btn_id){
    var btn = document.getElementById(str_btn_id);
    var tmp_index = btn.value.indexOf("(处理中,请等待...)");
    if(tmp_index>0){
        btn.value=btn.value.substr(0,tmp_index);
    }
    btn.disabled=false;
}
  
function waitingButton(str_btn_id){
    var btn = document.getElementById(str_btn_id);
    btn.value=btn.value+"(处理中,请等待...)";
    btn.disabled=true;
}

function enableButton(str_btn_id,str_label){
    var btn = document.getElementById(str_btn_id);
    if( typeof(str_label)!=undefined && str_label && str_label.length>0)
        btn.value=str_label;
    btn.disabled=false;
}
  
function disableButton(str_btn_id,str_label){
    var btn = document.getElementById(str_btn_id);
    if( typeof(str_label)!=undefined && str_label && str_label.length>0)
        btn.value=str_label;
    btn.disabled=true;
}

function hexToString(hex){  
　  var trimedStr = hex.trim();
　　var rawStr = trimedStr.substr(0,2).toLowerCase() === "0x" ? trimedStr.substr(2) : trimedStr;
　　var len = rawStr.length;
　　if(len % 2 !== 0) {
　　　　alert("Illegal Format ASCII Code!");
　　　　return "";
　　}
　　var curCharCode;
　　var resultStr = [];
　　for(var i = 0; i < len;i = i + 2) {
　　　　curCharCode = parseInt(rawStr.substr(i, 2), 16);
　　　　resultStr.push(String.fromCharCode(curCharCode));
　　}
　　return resultStr.join("");
}  

function stringToHex(str){
  var val="";
  for(var i = 0; i < str.length; i++){
      var tmp_str = str.charCodeAt(i).toString(16);
      if(tmp_str.length==1){
          tmp_str = '0'+tmp_str;
      }
      
      if(val == "")
          val = tmp_str;
      else
          val += tmp_str;
  }
  return val;
}

function setCookie(c_name, value, expiredays){
  var exdate=new Date();
  exdate.setDate(exdate.getDate() + expiredays);
  document.cookie=c_name+ "=" + escape(value) + ((expiredays==null) ? "" : ";expires="+exdate.toGMTString());
}

function getCookie(c_name){
  if (document.cookie.length>0){ 
    c_start=document.cookie.indexOf(c_name + "=");
    if (c_start!=-1){ 
      c_start=c_start + c_name.length+1;
      c_end=document.cookie.indexOf(";",c_start);
      if (c_end==-1) 
        c_end=document.cookie.length    
      return unescape(document.cookie.substring(c_start,c_end));
    } 
  }
  return "";
}

function utf16ToUtf8(s){
	if(!s){
		return;
	}
	
	var i, code, ret = [], len = s.length;
	for(i = 0; i < len; i++){
		code = s.charCodeAt(i);
		if(code > 0x0 && code <= 0x7f){
			//单字节
			//UTF-16 0000 - 007F
			//UTF-8  0xxxxxxx
			ret.push(s.charAt(i));
		}else if(code >= 0x80 && code <= 0x7ff){
			//双字节
			//UTF-16 0080 - 07FF
			//UTF-8  110xxxxx 10xxxxxx
			ret.push(
				//110xxxxx
				String.fromCharCode(0xc0 | ((code >> 6) & 0x1f)),
				//10xxxxxx
				String.fromCharCode(0x80 | (code & 0x3f))
			);
		}else if(code >= 0x800 && code <= 0xffff){
			//三字节
			//UTF-16 0800 - FFFF
			//UTF-8  1110xxxx 10xxxxxx 10xxxxxx
			ret.push(
				//1110xxxx
				String.fromCharCode(0xe0 | ((code >> 12) & 0xf)),
				//10xxxxxx
				String.fromCharCode(0x80 | ((code >> 6) & 0x3f)),
				//10xxxxxx
				String.fromCharCode(0x80 | (code & 0x3f))
			);
		}
	}
	
	return ret.join('');
}

function utf8ToUtf16(s){
	if(!s){
		return;
	}
	
	var i, codes, bytes, ret = [], len = s.length;
	for(i = 0; i < len; i++){
		codes = [];
		codes.push(s.charCodeAt(i));
		if(((codes[0] >> 7) & 0xff) == 0x0){
			//单字节  0xxxxxxx
			ret.push(s.charAt(i));
		}else if(((codes[0] >> 5) & 0xff) == 0x6){
			//双字节  110xxxxx 10xxxxxx
			codes.push(s.charCodeAt(++i));
			bytes = [];
			bytes.push(codes[0] & 0x1f);
			bytes.push(codes[1] & 0x3f);
			ret.push(String.fromCharCode((bytes[0] << 6) | bytes[1]));
		}else if(((codes[0] >> 4) & 0xff) == 0xe){
			//三字节  1110xxxx 10xxxxxx 10xxxxxx
			codes.push(s.charCodeAt(++i));
			codes.push(s.charCodeAt(++i));
			bytes = [];
			bytes.push((codes[0] << 4) | ((codes[1] >> 2) & 0xf));
			bytes.push(((codes[1] & 0x3) << 6) | (codes[2] & 0x3f));			
			ret.push(String.fromCharCode((bytes[0] << 8) | bytes[1]));
		}
	}
	return ret.join('');
}

//判断是否为ODIN根标识
function isRootODIN(uri){
    if(uri==null)
        return false;
    
    var parts = uri.split("/");
    
    if(parts.length==1)
        return true;
    
    if(parts.length>2)
        return false;
    
    if(parts[1].trim().length==0)
        return true;
    
    var parts2 = parts[1].split("#");
    if(parts2[0].trim().length==0)
        return true;
    else
        return false;
    
}

//获取当前时间戳（到秒值）
function getNowTimeStamp(){
    var timestamp1 = Date.parse( new Date());
    return timestamp1/1000;
}

//统一的提示信息显示方法
function commonAlert(obj){
    var str= typeof(obj)=="string" ? obj : JSON.stringify(obj);
    if(typeof(imToken)  !== 'undefined' ){
        imToken.callAPI('native.toastInfo', str);
    }else{
        alert(str);
    }
}

//采用H5本地存储保存数据
function getLocalConfigData(key,need_md5_hash){
    if(typeof(localStorage)!=="undefined")
    {
        try{
            need_md5_hash = typeof need_md5_hash !== 'undefined' ? need_md5_hash:false;
            if(need_md5_hash)
                key=CryptoJS.MD5(key);
            //console.log("getLocalConfigData CryptoJS.MD5="+key);
            // 是的! 支持 localStorage  sessionStorage 对象!
            return localStorage.getItem(key);
        }catch(e){
            console.log("getLocalConfigData() error:"+e);
            return null;
        }
    } else {
        // 抱歉! 不支持 web 存储。
        return null;
    }
}

//从H5本地存储读取数据
function saveLocalConfigData(key,value,need_md5_hash){
    if(typeof(localStorage)!=="undefined")
    {
        try{
            need_md5_hash = typeof need_md5_hash !== 'undefined' ? need_md5_hash:false;
            if(need_md5_hash)
                key=CryptoJS.MD5(key);
            //console.log("saveLocalConfigData CryptoJS.MD5="+key);
            // 是的! 支持 localStorage  sessionStorage 对象!
            return localStorage.setItem(key,value);
        }catch(e){
            console.log("saveLocalConfigData() error:"+e);
            return false;
        }
        /*
        try {
            localStorage.setItem(key, data);
        } catch (e) {
            if (e.code === 22 ) { //如果空间已满,则自动清理,待完善为清理最旧的
                localStorage.clear();
                //for(var i=localStorage.length - 1 ; i >=0; i--){
                //    console.log('第'+ (i+1) +'条数据的键值为：' + localStorage.key(i) +'，数据为：' + localStorage.getItem(localStorage.key(i)));
                //}
    
                return localStorage.setItem(key, data);
            }
        }
        */
    } else {
        // 抱歉! 不支持 web 存储。
        return false;
    }
}

//删除H5本地存储保存数据
function removeLocalConfigData(key,need_md5_hash){
    if(typeof(localStorage)!=="undefined")
    {
        try{
            need_md5_hash = typeof need_md5_hash !== 'undefined' ? need_md5_hash:false;
            if(need_md5_hash)
                key=CryptoJS.MD5(key);
            //console.log("removeLocalConfigData CryptoJS.MD5="+key);
            // 是的! 支持 localStorage  sessionStorage 对象!
            return localStorage.removeItem(key);
        }catch(e){
            console.log("removeLocalConfigData() error:"+e);
            return null;
        }
    } else {
        // 抱歉! 不支持 web 存储。
        return null;
    }
}

//获取网址中的查询参数
/*
function getQueryString(name) {
    let reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
    let r = window.location.search.substr(1).match(reg);
    if (r != null) {
        return decodeURIComponent(r[2]);
    };
    return null;
}
*/
function getQueryString(name) {
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
    var reg_rewrite = new RegExp("(^|/)" + name + "/([^/]*)(/|$)", "i");
    var r = window.location.search.substr(1).match(reg);
    var q = window.location.pathname.substr(1).match(reg_rewrite);
    if(r != null){
        return decodeURIComponent(r[2]);
    }else if(q != null){
        return decodeURIComponent(q[2]);
    }else{
        return null;
    }
}

/**
 * 压缩 ，注意先引用https://cdn.bootcss.com/pako/1.0.6/pako.min.js
 */
function compressStr(strNormalString) {
    console.log("压缩前长度：" + strNormalString.length);
    var strCompressedString = null;
    try{
        strCompressedString = pako.deflate(strNormalString, { to: 'string' });
    }catch(e){
        //console.log("compressStr() error:"+e);
        strCompressedString = strNormalString;
    }
    
    console.log("压缩后长度：" + strCompressedString.length);
    return strCompressedString;
}

/**
 * 解压缩
 */
function decompressStr(strCompressedString) {
    console.log("解压前长度：" + strCompressedString.length);
    var strNormalString = null;
    try{
        strNormalString = pako.inflate(strCompressedString, { to: 'string' });
    }catch(e){
        //console.log("decompressStr() error:"+e);
        strNormalString = strCompressedString;
    }
    
    console.log("解压后长度：" + strNormalString.length);
    return strNormalString;
}

/**
 * 将AJAX的结果转换为JSON对象
 */
function parseJsonObjFromAjaxResult(result){
    if(typeof result != 'string')
        return result;
    try{
        return JSON.parse( result );
    }catch(e){
        console.log("Meet invalid json string : "+result);
        return null;
    }
}


/**
 * 过滤敏感字符得到可安全加入网页显示的字符串
 */
function getSafeEchoText(str){
    if(typeof str != 'string')
        return "";
    
    if (str.length == 0) 
        return ""; 
    
    var s = ""; 
    s = str.replace(/&/g, "&amp;"); 
    s = s.replace(/</g, "&lt;"); 
    s = s.replace(/>/g, "&gt;"); 
    //s = s.replace(/ /g, "&nbsp;"); 
    s = s.replace(/\'/g, "&#39;"); 
    s = s.replace(/\"/g, "&quot;"); 
    //s = s.replace(/\n/g, "<br/>"); 
        
    return s; 
}


/**参数说明： 
 * 根据长度截取先使用字符串，超长部分追加… 
 * str 对象字符串 
 * len 目标字节长度 
 * 返回值： 处理结果字符串 
 */ 
function cutString(str, len) { 
   //length属性读出来的汉字长度为1 
   if(str.length*2 <= len) { 
     return str; 
   } 
   var strlen = 0; 
   var s = ""; 
   for(var i = 0;i < str.length; i++) { 
     s = s + str.charAt(i); 
     if (str.charCodeAt(i) > 128) { 
       strlen = strlen + 2; 
       if(strlen >= len){ 
         return s.substring(0,s.length-1) + "..."; 
       } 
     } else { 
       strlen = strlen + 1; 
       if(strlen >= len){ 
         return s.substring(0,s.length-2) + "..."; 
       } 
     } 
   } 
   return s; 
} 
