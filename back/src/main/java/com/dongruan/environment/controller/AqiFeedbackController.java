package com.dongruan.environment.controller;

import com.dongruan.environment.common.ResultVO;
import com.dongruan.environment.entity.AqiFeedback;
import com.dongruan.environment.service.IAqiFeedbackService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;


/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author weiqiang
 * @since 2026-09-03
 */
@RestController
@AllArgsConstructor
@RequestMapping("/aqiFeedback")
public class AqiFeedbackController {

    private final IAqiFeedbackService aqiFeedbackService;

    @GetMapping("/list")
    public ResultVO getAqiFeedbackList() {
        return new ResultVO(200,"查询成功",aqiFeedbackService.findAll());
    }

    /**
     * 根据id查询反馈信息
     * @param afId
     * @return
     */
//    @GetMapping("/find")    //  find?afid=1
    @GetMapping("/find/{afId}")  //RestFul 风格  、/find/1
    public ResultVO findById(@PathVariable Integer afId ){
        AqiFeedback feedback = aqiFeedbackService.getById(afId);
        return new ResultVO(200,"查询成功",feedback);
    }

    @GetMapping("/delete/{afId}")  //RestFul 风格  、/find/1
    public ResultVO delete(@PathVariable Integer afId ){
        boolean success = aqiFeedbackService.removeById(afId);
        return new ResultVO(200,"删除成功",success);
    }

    @PostMapping("/save")
    @ResponseBody
    public ResultVO save(@RequestBody AqiFeedback feedback){
        boolean success =   aqiFeedbackService.save( feedback);
        return new ResultVO(200,"保存成功",success);
    }

    @PostMapping("/update")
    @ResponseBody
    public ResultVO update(@RequestBody AqiFeedback feedback){
        boolean success = aqiFeedbackService.updateById(feedback);
        return new ResultVO(200,"更新成功",success);
    }
}
