package com.freelycar.saas.project.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.freelycar.saas.basic.wrapper.Constants;
import com.freelycar.saas.basic.wrapper.PageableTools;
import com.freelycar.saas.basic.wrapper.ResultCode;
import com.freelycar.saas.basic.wrapper.ResultJsonObject;
import com.freelycar.saas.exception.ArgumentMissingException;
import com.freelycar.saas.project.entity.CardService;
import com.freelycar.saas.project.entity.*;
import com.freelycar.saas.project.repository.SpecialOfferRepository;
import com.freelycar.saas.project.repository.StoreImgRepository;
import com.freelycar.saas.project.repository.StoreRepository;
import com.freelycar.saas.util.SpringContextUtils;
import com.freelycar.saas.util.UpdateTool;
import com.freelycar.saas.wechat.model.CouponInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author tangwei - Toby
 * @date 2018/11/28
 * @email toby911115@gmail.com
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class StoreService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private CardServiceService cardServiceService;

    @Autowired
    private CouponServiceService couponServiceService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private StoreImgRepository storeImgRepository;

    @Autowired
    private SpecialOfferRepository specialOfferRepository;


    /**
     * 新增或更新
     *
     * @param store 门店对象
     * @return Store
     */
    public Store saveOrUpdate(Store store) {
        if (null == store) {
            return null;
        }
        String id = store.getId();
        if (StringUtils.isEmpty(id)) {
            store.setDelStatus(Constants.DelStatus.NORMAL.isValue());
            store.setCreateTime(new Timestamp(System.currentTimeMillis()));
            store.setSort(this.generateSort());

            //TODO 新增门店成功后需要添加一个orderSn规则
        } else {
            Store source = storeRepository.findById(id).orElse(null);
            if (null == source) {
                return null;
            }
            UpdateTool.copyNullProperties(source, store);
        }
        return storeRepository.saveAndFlush(store);
    }


    /**
     * 自动生成排序号
     *
     * @return long
     */
    private long generateSort() {
        Store store = storeRepository.findTopByDelStatusAndSortIsNotNullOrderBySortDesc(Constants.DelStatus.NORMAL.isValue());
        if (null == store) {
            return 10L;
        }
        return store.getSort() + 10;
    }


    /**
     * 删除门店
     *
     * @param id
     * @return
     */
    public ResultJsonObject delete(String id) {
        if (StringUtils.isEmpty(id)) {
            return ResultJsonObject.getErrorResult(null, "删除失败：id" + ResultCode.PARAM_NOT_COMPLETE.message());
        }
        int res = storeRepository.delById(id);
        if (res == 1) {
            return ResultJsonObject.getDefaultResult(id);
        }
        return ResultJsonObject.getErrorResult(null, ResultCode.RESULT_DATA_NONE.message());
    }

    /**
     * 批量删除门店
     *
     * @param ids
     * @return
     */
    public ResultJsonObject delByIds(String ids) {
        if (StringUtils.isEmpty(ids)) {
            return ResultJsonObject.getErrorResult(null, "删除失败：ids" + ResultCode.PARAM_NOT_COMPLETE.message());
        }
        String[] idsList = ids.split(",");
        for (String id : idsList) {
            storeRepository.delById(id);
        }
        return ResultJsonObject.getDefaultResult(null);
    }

    /**
     * 分页查询（包含“门店名称”的模糊查询）
     *
     * @param name
     * @param currentPage
     * @param pageSize
     * @return
     */
    public Page<Store> list(String name, Integer currentPage, Integer pageSize) {
        return storeRepository.findStoreByDelStatusAndNameContainingOrderBySortAsc(Constants.DelStatus.NORMAL.isValue(), name, PageableTools.basicPage(currentPage, pageSize));
    }


    /**
     * 查询所有门店信息（包含模糊查询）
     *
     * @param name
     * @return
     */
    public List<Store> findAllByName(String name) {
        return storeRepository.findStoreByDelStatusAndNameContainingOrderBySortAsc(Constants.DelStatus.NORMAL.isValue(), name);
    }


    /**
     * 通过ID查询门店信息
     *
     * @param id
     * @return
     */
    public ResultJsonObject getDetail(String id) {
        Optional<Store> optionalStore = storeRepository.findById(id);
        if (!optionalStore.isPresent()) {
            return ResultJsonObject.getErrorResult(id, "查询失败，为找到id为：" + id + "的门店信息");
        }


        //获取在售的会员卡
        List<CardService> cardServices = cardServiceService.findOnSaleCards(id);

        //获取在售的优惠券
        List<CouponInfo> couponServices;
        try {
            couponServices = couponServiceService.findOnSaleCoupons(id);
        } catch (ArgumentMissingException e) {
            logger.error(e.getMessage(), e);
            e.printStackTrace();
            couponServices = new ArrayList<>();
        }

        //获取门店展示的服务项目
        List<Project> projectsList = projectService.getShowProjects(id);
        JSONArray projects = new JSONArray();
        Map<String, String> typeInfos = new HashMap<>();
        List<String> projectTypeIdList = new ArrayList<>();
        if (!projectsList.isEmpty()) {
            //统计查处的项目有几个分类
            for (Project project : projectsList) {
                String projectTypeId = project.getProjectTypeId();
                String projectTypeName = project.getProjectTypeName();
                if (!typeInfos.containsKey(projectTypeId)) {
                    typeInfos.put(projectTypeId, projectTypeName);
                    projectTypeIdList.add(projectTypeId);
                }
            }
            //按分类去处理项目
            for (String projectTypeId : projectTypeIdList) {
                if (StringUtils.hasText(projectTypeId)) {
                    JSONObject typeJSON = new JSONObject();
                    typeJSON.put("projectTypeId", projectTypeId);
                    typeJSON.put("projectTypeName", typeInfos.get(projectTypeId));
                    JSONArray typeJSONArr = new JSONArray();
                    for (Project projectObj : projectsList) {
                        if (projectTypeId.equals(projectObj.getProjectTypeId())) {
                            typeJSONArr.add(projectObj);
                        }
                    }
                    typeJSON.put("projectInfos", typeJSONArr);
                    projects.add(typeJSON);
                }
            }
        }

        //获取门店的轮播图
        List<StoreImg> storeImgs = getImgList(id);


        JSONObject jsonObject = new JSONObject();
        jsonObject.put("store", optionalStore.get());
        jsonObject.put("cardServices", cardServices);
        jsonObject.put("couponServices", couponServices);
        jsonObject.put("projects", projects);
        jsonObject.put("storeImgs", storeImgs);

        return ResultJsonObject.getDefaultResult(jsonObject);
    }

    public ResultJsonObject listAllStoreLocation() {
        return ResultJsonObject.getDefaultResult(this.findAllEffectiveStores());
    }

    /**
     * 查询所以有效的门店
     *
     * @return
     */
    public List<Store> findAllEffectiveStores() {
        return storeRepository.findAllByDelStatusOrderBySortAsc(Constants.DelStatus.NORMAL.isValue());
    }


    public ResultJsonObject uploadPicture(MultipartFile file, HttpServletRequest request) {
        if (null == file || file.isEmpty()) {
            return ResultJsonObject.getCustomResult(null, ResultCode.PARAM_IS_BLANK);
        }
        //保存时的文件名
        DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        Calendar calendar = Calendar.getInstance();
        String dateName = df.format(calendar.getTime()) + file.getOriginalFilename();

        System.out.println(dateName);

        //保存文件的绝对路径
        WebApplicationContext webApplicationContext = (WebApplicationContext) SpringContextUtils.applicationContext;
        ServletContext servletContext = webApplicationContext.getServletContext();
        assert servletContext != null;
        String realPath = servletContext.getRealPath("/");
        String filePath = realPath + "WEB-INF" + File.separator + "classes" + File.separator + "static" + File.separator + "resource" + File.separator + dateName;
        System.out.println("绝对路径:" + filePath);

        File newFile = new File(filePath);

        //MultipartFile的方法直接写文件
        try {

            //上传文件
            file.transferTo(newFile);

            //数据库存储的相对路径
            String projectPath = servletContext.getContextPath();
            String contextPath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + projectPath;
            String url = contextPath + "/resource/" + dateName;
            System.out.println("相对路径:" + url);
            //TODO 文件名与文件URL存入数据库表

            return ResultJsonObject.getDefaultResult(url);
        } catch (IllegalStateException | IOException e) {
            logger.error(e.getMessage(), e);
            e.printStackTrace();
        }
        return ResultJsonObject.getErrorResult(null, "上传失败");
    }

    /**
     * 查询门店宣传图
     *
     * @param storeId
     * @return
     */
    public ResultJsonObject getImgs(String storeId) {
        if (StringUtils.hasText(storeId)) {
            return ResultJsonObject.getDefaultResult(getImgList(storeId));
        }
        return ResultJsonObject.getCustomResult(null, ResultCode.PARAM_NOT_COMPLETE);
    }

    /**
     * 查询门店宣传图列表
     *
     * @param storeId
     * @return
     */
    public List<StoreImg> getImgList(String storeId) {
        //获取门店的轮播图
        return storeImgRepository.findByStoreIdAndDelStatusOrderByCreateTimeAsc(storeId, Constants.DelStatus.NORMAL.isValue());
    }

    public ResultJsonObject listWeCahtImgs() {
        List<SpecialOffer> specialOffers = specialOfferRepository.findByDelStatusOrderBySort(Constants.DelStatus.NORMAL.isValue());
        return ResultJsonObject.getDefaultResult(specialOffers);
    }

}
