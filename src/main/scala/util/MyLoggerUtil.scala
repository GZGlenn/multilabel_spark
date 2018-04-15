package util

import org.slf4j.{Logger, LoggerFactory}

/**
  * Created by glennbinhu on 2017/8/24.
  */
object MyLoggerUtil {
    /**
      * 获得Logger
      *
      * @param clazz 日志发出的类
      * @return Logger
      */
    def get(clazz: Class[_]): Logger = LoggerFactory.getLogger(clazz)

    /**
      * 获得Logger
      *
      * @param name 自定义的日志发出者名称
      * @return Logger
      */
    def get(name: String): Logger = LoggerFactory.getLogger(name)

    /**
      * @return 获得日志，自动判定日志发出者
      */
    def get: Logger = {
      val stackTrace = Thread.currentThread.getStackTrace
      LoggerFactory.getLogger(stackTrace(2).getClassName)
    }

    /**
      * Trace等级日志，小于debug<br>
      * 由于动态获取Logger，效率较低，建议在非频繁调用的情况下使用！！
      *
      * @param format    格式文本，{} 代表变量
      * @param arguments 变量对应的参数
      */
    //----------------------------------------------------------- Logger method start
    //------------------------ Trace
    def trace(format: String, arguments: Any*): Unit = {
      trace(innerGet, format, arguments)
    }

    /**
      * Trace等级日志，小于Debug
      *
      * @param log       日志对象
      * @param format    格式文本，{} 代表变量
      * @param arguments 变量对应的参数
      */
    def trace(log: Logger, format: String, arguments: Any*): Unit = {
      log.trace(format, arguments)
    }

    /**
      * Debug等级日志，小于Info<br>
      * 由于动态获取Logger，效率较低，建议在非频繁调用的情况下使用！！
      *
      * @param format    格式文本，{} 代表变量
      * @param arguments 变量对应的参数
      */
    //------------------------ debug
    def debug(format: String, arguments: Any*): Unit = {
      debug(innerGet, format, arguments)
    }

    /**
      * Debug等级日志，小于Info
      *
      * @param log       日志对象
      * @param format    格式文本，{} 代表变量
      * @param arguments 变量对应的参数
      */
    def debug(log: Logger, format: String, arguments: Any*): Unit = {
      log.debug(format, arguments)
    }

    /**
      * Info等级日志，小于Warn<br>
      * 由于动态获取Logger，效率较低，建议在非频繁调用的情况下使用！！
      *
      * @param format    格式文本，{} 代表变量
      * @param arguments 变量对应的参数
      */
    //------------------------ info
    def info(format: String, arguments: Any*): Unit = {
      info(innerGet, format, arguments)
    }

    /**
      * Info等级日志，小于Warn
      *
      * @param log       日志对象
      * @param format    格式文本，{} 代表变量
      * @param arguments 变量对应的参数
      */
    def info(log: Logger, format: String, arguments: Any*): Unit = {
      log.info(format, arguments)
    }

    /**
      * Warn等级日志，小于Error<br>
      * 由于动态获取Logger，效率较低，建议在非频繁调用的情况下使用！！
      *
      * @param format    格式文本，{} 代表变量
      * @param arguments 变量对应的参数
      */
    //------------------------ warn
    def warn(format: String, arguments: Any*): Unit = {
      warn(innerGet, format, arguments)
    }

    /**
      * Warn等级日志，小于Error
      *
      * @param log       日志对象
      * @param format    格式文本，{} 代表变量
      * @param arguments 变量对应的参数
      */
    def warn(log: Logger, format: String, arguments: Any*): Unit = {
      log.warn(format, arguments)
    }

    /**
      * Warn等级日志，小于Error<br>
      * 由于动态获取Logger，效率较低，建议在非频繁调用的情况下使用！！
      *
      * @param e         需在日志中堆栈打印的异常
      * @param format    格式文本，{} 代表变量
      * @param arguments 变量对应的参数
      */
    def warn(e: Throwable, format: String, arguments: Any*): Unit = {
      warn(innerGet, e, formatMed(format, arguments))
    }

    /**
      * Warn等级日志，小于Error
      *
      * @param log       日志对象
      * @param e         需在日志中堆栈打印的异常
      * @param format    格式文本，{} 代表变量
      * @param arguments 变量对应的参数
      */
    def warn(log: Logger, e: Throwable, format: String, arguments: Any*): Unit = {
      log.warn(formatMed(format, arguments), e)
    }

    /**
      * Error等级日志<br>
      * 由于动态获取Logger，效率较低，建议在非频繁调用的情况下使用！！
      *
      * @param format    格式文本，{} 代表变量
      * @param arguments 变量对应的参数
      */
    //------------------------ error
    def error(format: String, arguments: Any*): Unit = {
      error(innerGet, format, arguments)
    }

    /**
      * Error等级日志<br>
      *
      * @param log       日志对象
      * @param format    格式文本，{} 代表变量
      * @param arguments 变量对应的参数
      */
    def error(log: Logger, format: String, arguments: Any*): Unit = {
      log.error(format, arguments)
    }

    /**
      * Error等级日志<br>
      * 由于动态获取Logger，效率较低，建议在非频繁调用的情况下使用！！
      *
      * @param e         需在日志中堆栈打印的异常
      * @param format    格式文本，{} 代表变量
      * @param arguments 变量对应的参数
      */
    def error(e: Throwable, format: String, arguments: Any*): Unit = {
      error(innerGet, e, formatMed(format, arguments))
    }

    /**
      * Error等级日志<br>
      * 由于动态获取Logger，效率较低，建议在非频繁调用的情况下使用！！
      *
      * @param log       日志对象
      * @param e         需在日志中堆栈打印的异常
      * @param format    格式文本，{} 代表变量
      * @param arguments 变量对应的参数
      */
    def error(log: Logger, e: Throwable, format: String, arguments: Any*): Unit = {
      log.error(formatMed(format, arguments), e)
    }

    /**
      * 格式化文本
      *
      * @param template 文本模板，被替换的部分用 {} 表示
      * @param values   参数值
      * @return 格式化后的文本
      */
    //----------------------------------------------------------- Logger method end
    //----------------------------------------------------------- Private method start
    private def formatMed(template: String, values: Any*) = String.format(template.replace("{}", "%s"), values)

    private def innerGet = {
      val stackTrace = Thread.currentThread.getStackTrace
      LoggerFactory.getLogger(stackTrace(3).getClassName)
    }

}
