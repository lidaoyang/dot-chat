const LoggerLevel = {
    error: 0,
    warn: 1,
    info: 2,
    debug: 3
};

class Logger {
    #level = LoggerLevel.debug;

    constructor(level) {
        this.#level = LoggerLevel[level];
    }

    log(level, message, ...args) {
        if (level <= this.#level) {
            switch (level) {
                case LoggerLevel.error:
                    console.error(`[error] ${moment().format('YYYY-MM-DD HH:mm:ss')} ====> ${message}`, ...args);
                    break;
                case LoggerLevel.warn:
                    console.warn(`[warn] ${moment().format('YYYY-MM-DD HH:mm:ss')} ====> ${message}`, ...args);
                    break;
                case LoggerLevel.info:
                    console.info(`[info] ${moment().format('YYYY-MM-DD HH:mm:ss')} ====> ${message}`, ...args);
                    break;
                case LoggerLevel.debug:
                    console.debug(`[debug] ${moment().format('YYYY-MM-DD HH:mm:ss')} ====> ${message}`, ...args);
                    break;
                default:
                    break;
            }
        }
    }

    debug(message, ...args) {
        this.log(LoggerLevel.debug, message, ...args);
    }

    info(message, ...args) {
        this.log(LoggerLevel.info, message, ...args);
    }

    warn(message, ...args) {
        this.log(LoggerLevel.warn, message, ...args);
    }

    error(message, ...args) {
        this.log(LoggerLevel.error, message, ...args);
    }
}

