package com.csquare.lc.ms.orders.kafka.service;

import com.csquare.lc.ms.orders.kafka.service.interfaces.NonRilService;
import com.csquare.lc.ms.orders.lib.model.CustomerNameEnum;
import com.csquare.lc.ms.orders.lib.model.LcStoreSyncDetails;
import com.csquare.lc.ms.orders.lib.model.OrderStatusEnum;
import com.csquare.lc.ms.orders.lib.model.C2codeType;
import com.csquare.lc.ms.orders.lib.repos.C2codeTypeRepository;
import com.csquare.lc.ms.orders.lib.repos.LcStoreSyncDetailsRepository;
import com.csquare.lc.ms.orders.lib.repos.OrderMstRepository;
import com.csquare.ms.lib.services.BaseServicesImpl;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

@Log4j2
@Service
public class NonRilServiceImpl extends BaseServicesImpl implements NonRilService {
    @Autowired
    private LcStoreSyncDetailsRepository lcStoreSyncDetailsRepository;

    @Autowired
    private C2codeTypeRepository c2codeTypeRepository;

    @Autowired
    private OrderMstRepository orderMstRepository;

    @Override
    public void saveLcStoreSyncDetails(String c2Code, String url) {
        Optional<LcStoreSyncDetails> lcStoreSyncDetailsOpt = lcStoreSyncDetailsRepository.findById(c2Code);
        LcStoreSyncDetails lcStoreSyncDetails = null;
        if (lcStoreSyncDetailsOpt.isPresent())
            lcStoreSyncDetails = lcStoreSyncDetailsOpt.get();
        else {
            lcStoreSyncDetails = new LcStoreSyncDetails();
            lcStoreSyncDetails.setC2code(c2Code);
        }
        lcStoreSyncDetails.setTLastSeen(helper.getCurrentTime());
        lcStoreSyncDetails.setCLastApiType(url);
        if (!helper.isNull(lcStoreSyncDetails.getNPendingOrdersCount()) && lcStoreSyncDetails.getNPendingOrdersCount() > 0)
            lcStoreSyncDetails.setNPendingOrdersCount(lcStoreSyncDetails.getNPendingOrdersCount() + 1);
        else {
            Long orderCount = orderMstRepository.getOrderCount(c2Code, OrderStatusEnum.OP.name());
            lcStoreSyncDetails.setNPendingOrdersCount(orderCount);
        }
        lcStoreSyncDetailsRepository.save(lcStoreSyncDetails);
    }

    @Override
    public String getC2CodeTypeName(String c2code) {
        Optional<C2codeType> c2codeType = c2codeTypeRepository.findById(c2code);
        return c2codeType.isPresent() ? c2codeType.get().getCCustomerType() : "";
    }

}
