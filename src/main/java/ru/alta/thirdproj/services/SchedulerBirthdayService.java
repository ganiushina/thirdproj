package ru.alta.thirdproj.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.alta.thirdproj.repositories.userbirthday.BirthDayRepositories;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;

@Service
public class SchedulerBirthdayService implements Runnable {
    public static final int SLEEP_TIME = 3600000;
    public static final int HOUR_TO_START_USER_BIRTHDAY = 8;
    public static final int HOUR_TO_START_USER_BIRTHDAY_PER_MONTH = 9;
    public static final int HOUR_TO_START_CLIENT_BIRTHDAY = 7;

    protected static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private Object mutex = new Object();
    private boolean findBirthDayStarted = false;

    private BirthDayRepositories birthDayRepositories;

    @Autowired
    public void BirthdayGrabberService(BirthDayRepositories birthDayRepositories) {
        this.birthDayRepositories = birthDayRepositories;
    }

    public void findBirthDayStarted(){
        synchronized (mutex){
            findBirthDayStarted = true;
        }
    }

    public void findBirthDayStopped(){
        synchronized (mutex){
            findBirthDayStarted = false;
        }
    }

    public boolean isFindBirthDayStarted(){
        synchronized (mutex){
            return findBirthDayStarted;
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                if(!findBirthDayStarted) {
                    if (shouldStartGrabber()) {
                     //   BirthdayGrabberService birthdayGrabber = new BirthdayGrabberService();
                      //  birthdayGrabber.start(checkHourToStart());
                    }
                }
                Thread.sleep(SLEEP_TIME);
            } catch (Exception e) {
                log.error("===> Error on BirthDayGrabber scheduler {}",e);
            }
        }
    }

    /**
     * if hour is 8 then start user birthday find dayly. type 1
     * if hour is 9 then start user birthday find first day month. type 2
     * if hour is 7 then start client birthday find dayly. type 3
     *
     * @return
     */
    private boolean shouldStartGrabber() {
        log.info("===> Checking if should start BirthDayGrabber");
        boolean startedToday = false;
        boolean result = false;
        log.info("===> BirthDayGrabber not started today");

        if (checkHourToStart()==1) {
            startedToday = birthDayRepositories.getDayGrabberStatistic(LocalDate.now(), 1);
            if (!startedToday) {
                log.info("===> Proper hour for user birthday dayly");
                result = true;
            } else {
                log.info("===> Not proper hour for user birthday dayly");
            }
        } else

            if (checkHourToStart()==100) {
                startedToday = birthDayRepositories.getDayGrabberStatistic(LocalDate.now(), 2);
                if (!startedToday) {
                    log.info("===> Proper hour for birthday find day month");
                    result = true;
                } else {
                    log.info("===> Not proper hour for birthday first day month");
                }
            } else

                if (checkHourToStart()==0) {
                    startedToday = birthDayRepositories.getDayGrabberStatistic(LocalDate.now(), 3);
                    if (!startedToday) {
                        log.info("===> Proper hour for client birthday dayly");
                        result = true;
                    } else {
                        log.info("===> Not proper hour for client birthday dayly");
                    }
                }
                 else {
                    log.info("===> BirthDayGrabber already started today");
                }
        return result;
    }

    private Integer checkHourToStart(){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        if (HOUR_TO_START_USER_BIRTHDAY == calendar.get(Calendar.HOUR_OF_DAY)) {
            return 1;
        }
        if (HOUR_TO_START_USER_BIRTHDAY_PER_MONTH == calendar.get(Calendar.HOUR_OF_DAY)) {
            return 100;
        }
        if (HOUR_TO_START_CLIENT_BIRTHDAY == calendar.get(Calendar.HOUR_OF_DAY)) {
            return 0;
        }
        return null;
    }

}
