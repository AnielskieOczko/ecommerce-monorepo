// src/utils/logger.ts
enum LogLevel {
  ERROR = 'ERROR',
  WARN = 'WARN',
  INFO = 'INFO',
  DEBUG = 'DEBUG',
}

class Logger {
  private static isProduction = process.env.REACT_APP_ENVIRONMENT === 'production';

  private static log(level: LogLevel, message: string, ...optionalParams: any[]): void {
    if (this.isProduction && level !== LogLevel.ERROR) return;

    const timestamp = new Date().toISOString();
    const formattedMessage = `[${timestamp}] [${level}] ${message}`;

    switch (level) {
      case LogLevel.ERROR:
        console.error(formattedMessage, ...optionalParams);
      break;
      case LogLevel.WARN:
        console.warn(formattedMessage, ...optionalParams);
      break;
      case LogLevel.INFO:
        console.info(formattedMessage, ...optionalParams);
      break;
      case LogLevel.DEBUG:
      console.debug(formattedMessage, ...optionalParams);
      break;
    }
  }

  static error(message: string, ...optionalParams: any[]): void {
    this.log(LogLevel.ERROR, message, ...optionalParams);
  }

  static warn(message: string, ...optionalParams: any[]): void {
    this.log(LogLevel.WARN, message, ...optionalParams);
  }

  static info(message: string, ...optionalParams: any[]): void {
    this.log(LogLevel.INFO, message, ...optionalParams);
  }

  static debug(message: string, ...optionalParams: any[]): void {
    this.log(LogLevel.DEBUG, message, ...optionalParams);
  }
}

export default Logger;
