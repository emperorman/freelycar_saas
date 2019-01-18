package com.freelycar.saas.project.service;

import com.freelycar.saas.basic.wrapper.Constants;
import com.freelycar.saas.basic.wrapper.ResultJsonObject;
import com.freelycar.saas.project.entity.Card;
import com.freelycar.saas.project.entity.Client;
import com.freelycar.saas.project.entity.ConsumerOrder;
import com.freelycar.saas.project.repository.CardRepository;
import com.freelycar.saas.project.repository.CardServiceRepository;
import com.freelycar.saas.project.repository.ClientRepository;
import com.freelycar.saas.project.repository.ConsumerOrderRepository;
import com.freelycar.saas.util.TimestampUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.EntityNotFoundException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

/**
 * @author tangwei - Toby
 * @date 2018-12-27
 * @email toby911115@gmail.com
 */
@Service
@Transactional
public class CardService {
    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private CardServiceRepository cardServiceRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ConsumerOrderRepository consumerOrderRepository;

    /**
     * 办理会员卡
     *
     * @param card
     * @return
     */
    public ResultJsonObject handleCard(Card card) {
        if (null == card) {
            return ResultJsonObject.getErrorResult(null, "开卡失败！参数card为Null！");
        }

        /* 验证非空 */
        String clientId = card.getClientId();
        String cardServiceId = card.getCardServiceId();
        String cardNumber = card.getCardNumber();

        if (StringUtils.isEmpty(clientId)) {
            return ResultJsonObject.getErrorResult(null, "开卡失败！clientId为Null！");
        }

        if (StringUtils.isEmpty(cardServiceId)) {
            return ResultJsonObject.getErrorResult(null, "开卡失败！cardServiceId为Null！");
        }

        if (StringUtils.isEmpty(cardNumber)) {
            return ResultJsonObject.getErrorResult(null, "开卡失败！cardNumber为Null！");
        }

        //验证卡号是否重复
        if (this.isCardNumberRepeat(card)) {
            return ResultJsonObject.getErrorResult(null, "开卡失败！卡号重复！如有疑问，请联系管理员！");
        }

        //查询会员卡销售信息
        com.freelycar.saas.project.entity.CardService cardServiceObject = cardServiceRepository.getOne(cardServiceId);
        if (null == cardServiceObject) {
            return ResultJsonObject.getErrorResult(null, "开卡失败！未查询到选择的卡类信息！如有疑问，请联系管理员！");
        }

        //验证对应的用户对象是否存在
        Optional<Client> clientOptional = clientRepository.findById(clientId);
        if (!clientOptional.isPresent()) {
            return ResultJsonObject.getErrorResult(null, "开卡失败！未查询到登记的用户信息！如有疑问，请联系管理员！");
        }

        Timestamp currentTime = new Timestamp(System.currentTimeMillis());

        //为字段赋默认值
        card.setDelStatus(Constants.DelStatus.NORMAL.isValue());
        card.setCreateTime(currentTime);
        card.setPayDate(currentTime);
        card.setBalance(cardServiceObject.getActualPrice());
        card.setActualPrice(cardServiceObject.getActualPrice());
        card.setFailed(false);
        card.setName(cardServiceObject.getName());
        card.setPrice(cardServiceObject.getPrice());
        card.setExpirationDate(TimestampUtil.getExpirationDateForYear(cardServiceObject.getValidTime()));

        Card cardRes = cardRepository.saveAndFlush(card);

        //更新用户“是否会员”的标记位
        Client client = clientOptional.get();
        client.setMember(true);
        client.setMemberDate(currentTime);
        Client clientRes = clientRepository.save(client);

        // 办卡成功后需要自动添加一条订单，且是自动结算的
        this.autoGenerateOrderForHandleCard(cardRes, clientRes);

        return ResultJsonObject.getDefaultResult(cardRes);
    }

    /**
     * 会员卡号是否重复
     *
     * @param card
     * @return
     */
    private boolean isCardNumberRepeat(Card card) {
        List<Card> cardList = cardRepository.findByCardNumberAndDelStatusAndStoreId(card.getCardNumber(), Constants.DelStatus.NORMAL.isValue(), card.getStoreId());
        return !cardList.isEmpty();
    }


    /**
     * 获取会员卡详情
     *
     * @param id
     * @return
     */
    public ResultJsonObject getDetail(String id) {
        return ResultJsonObject.getDefaultResult(cardRepository.findById(id));
    }

    /**
     * 会员卡结算业务
     *
     * @param cardId
     * @param amount
     * @throws EntityNotFoundException
     * @throws IllegalArgumentException
     */
    public void cardSettlement(String cardId, float amount) throws EntityNotFoundException, IllegalArgumentException {
        if (amount <= 0) {
            throw new IllegalArgumentException("扣除金额必须是正数！");
        }
        Card card = cardRepository.getOne(cardId);
        Float balance = card.getBalance();
        if (balance < amount) {
            throw new IllegalArgumentException("扣除金额大于卡内余额！");
        }
        card.setBalance(balance - amount);
        cardRepository.save(card);
    }

    /**
     * 办卡成功后需要自动添加一条订单，且是自动结算的
     *
     * @param card
     * @param client
     */
    public void autoGenerateOrderForHandleCard(Card card, Client client) {
        ConsumerOrder cardOrder = new ConsumerOrder();
        cardOrder.setDelStatus(Constants.DelStatus.NORMAL.isValue());
        cardOrder.setCreateTime(card.getCreateTime());
        cardOrder.setPayState(Constants.PayState.FINISH_PAY.getValue());
        cardOrder.setOrderType(Constants.OrderType.CARD.getValue());
        cardOrder.setCardOrCouponId(card.getId());
        cardOrder.setClientId(card.getClientId());
        cardOrder.setTotalPrice(card.getPrice().doubleValue());
        cardOrder.setActualPrice(card.getPrice().doubleValue());

        cardOrder.setClientName(client.getName());
        cardOrder.setPhone(client.getPhone());
        cardOrder.setMember(client.getMember());
        cardOrder.setGender(client.getGender());
        cardOrder.setStoreId(client.getStoreId());

        consumerOrderRepository.save(cardOrder);
    }
}
