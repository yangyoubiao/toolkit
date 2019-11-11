package com.yangyoubiao.www.shell;

import java.io.Serializable;

/**实时输出一行行结果（有一行就输出一行）*/
public interface ExecuteResponeLine extends Serializable{
	/**输出一行结果*/
	void printLine(String line)throws Exception;
}
