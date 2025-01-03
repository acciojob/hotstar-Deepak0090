package com.driver.services;


import com.driver.EntryDto.SubscriptionEntryDto;
import com.driver.model.Subscription;
import com.driver.model.SubscriptionType;
import com.driver.model.User;
import com.driver.repository.SubscriptionRepository;
import com.driver.repository.UserRepository;
import net.bytebuddy.implementation.bytecode.Throw;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class SubscriptionService {

    @Autowired
    SubscriptionRepository subscriptionRepository;

    @Autowired
    UserRepository userRepository;

    public Integer buySubscription(SubscriptionEntryDto subscriptionEntryDto) {

        //Save The subscription Object into the Db and return the total Amount that user has to pay
        User user = userRepository.findById(subscriptionEntryDto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Subscription subscription = new Subscription();
        subscription.setId(subscriptionEntryDto.getUserId());
        subscription.setSubscriptionType(subscriptionEntryDto.getSubscriptionType());
        subscription.setNoOfScreensSubscribed(subscriptionEntryDto.getNoOfScreensRequired());

        int totoalAmount = getBasePrice(subscriptionEntryDto.getSubscriptionType(),subscriptionEntryDto.getNoOfScreensRequired());
        subscription.setTotalAmountPaid(totoalAmount);


        subscriptionRepository.save(subscription);
        return totoalAmount;
    }

    public Integer upgradeSubscription(Integer userId) throws Exception {

        //If you are already at an ElITE subscription : then throw Exception ("Already the best Subscription")
        //In all other cases just try to upgrade the subscription and tell the difference of price that user has to pay
        //update the subscription in the repository
        Subscription subscription = subscriptionRepository.findById(userId)
        .orElseThrow(()->new Exception("Subscription not found"));

        if (subscription.getSubscriptionType()==SubscriptionType.ELITE){
            throw new Exception("Already the best Subscription");
        }
            SubscriptionType currentType = subscription.getSubscriptionType();
         SubscriptionType nextType = currentType==SubscriptionType.BASIC ?SubscriptionType.PRO :SubscriptionType.ELITE;
         int oldPrice = subscription.getTotalAmountPaid();
         int newPrice = getBasePrice(nextType, subscription.getNoOfScreensSubscribed());
         subscription.setSubscriptionType(nextType);
         subscription.setTotalAmountPaid(newPrice);
         subscriptionRepository.save(subscription);
        return newPrice-oldPrice;
    }

    public Integer calculateTotalRevenueOfHotstar() {

        //We need to find out total Revenue of hotstar : from all the subscriptions combined
        //Hint is to use findAll function from the SubscriptionDb
        List<Subscription> subscriptions= subscriptionRepository.findAll();

        return subscriptions.stream()
                .mapToInt(Subscription::getTotalAmountPaid)
                .sum();
    }
    public int getBasePrice(SubscriptionType subscriptionType, int noOfScreensRequired){
        int basePrice =0;

        switch (subscriptionType){
            case BASIC:
                basePrice=500+(noOfScreensRequired*200);
                break;
            case PRO:
                basePrice=800+(noOfScreensRequired*250);
                break;
            case ELITE:
                basePrice=1000+(noOfScreensRequired*300);
                break;
            default:
                throw new IllegalArgumentException("Unknown subscription type");
        }
        return basePrice;
    }
}

