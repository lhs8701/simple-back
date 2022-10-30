package team7.simple.domain.unit;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import team7.simple.domain.answer.dto.AnswerRequestDto;
import team7.simple.domain.answer.dto.AnswerUpdateParam;
import team7.simple.domain.answer.service.AnswerService;
import team7.simple.domain.question.dto.QuestionRequestDto;
import team7.simple.domain.question.dto.QuestionUpdateParam;
import team7.simple.domain.question.service.QuestionService;
import team7.simple.domain.unit.dto.UnitRequestDto;
import team7.simple.domain.unit.dto.UnitResponseDto;
import team7.simple.domain.unit.dto.UnitUpdateParam;
import team7.simple.domain.unit.service.UnitService;
import team7.simple.global.common.response.service.ResponseService;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
public class UnitController {
    private final UnitService unitService;
    private final QuestionService questionService;
    private final AnswerService answerService;

    @PostMapping("open/course/unit")
    public ResponseEntity<?> uploadUnit(@RequestPart @Valid UnitRequestDto unitRequestDto, @RequestPart MultipartFile file) {
        return new ResponseEntity<>(unitService.createUnit(unitRequestDto, file), HttpStatus.OK);
    }

    /*테스트용*/
    @PostMapping("open/course/unit/local")
    public ResponseEntity<?> uploadUnitLocal(@RequestBody @Valid UnitRequestDto unitRequestDto) {
        return new ResponseEntity<>(unitService.createUnitLocal(unitRequestDto), HttpStatus.OK);
    }


    @PatchMapping("/open/course/unit/{unitId}")
    public ResponseEntity<?> updateUnit(@RequestBody @Valid UnitUpdateParam unitUpdateParam) {
        return new ResponseEntity<>(unitService.updateUnit(unitUpdateParam), HttpStatus.OK);
    }

    @DeleteMapping("/open/course/unit/{unitId}")
    public ResponseEntity<?> deleteUnit(@PathVariable Long unitId) {
        unitService.deleteUnit(unitId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/front/course/unit/{unitId}")
    public ResponseEntity<?> getUnitInfo(@PathVariable Long unitId) {
        return new ResponseEntity<>(unitService.getUnitInfo(unitId), HttpStatus.OK);
    }

    @PostMapping("/front/course/unit/{unitId}/question")
    public ResponseEntity<?> uploadQuestion(@PathVariable Long unitId, @Valid QuestionRequestDto questionRequestDto) {
        return new ResponseEntity<>(questionService.createQuestion(unitId, questionRequestDto), HttpStatus.OK);
    }

    @PatchMapping("/front/course/unit/question/{questionId}")
    public ResponseEntity<?> updateQuestion(@RequestBody @Valid QuestionUpdateParam questionUpdateParam) {
        return new ResponseEntity<>(questionService.updateQuestion(questionUpdateParam), HttpStatus.OK);
    }

    @DeleteMapping("/front/course/unit/question/{questionId}")
    public ResponseEntity<?> deleteQuestion(@PathVariable Long questionId) {
        questionService.deleteQuestion(questionId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/front/course/unit/question/{questionId}")
    public ResponseEntity<?> getQuestionInfo(@PathVariable Long questionId) {
        return new ResponseEntity<>(questionService.getQuestionInfo(questionId), HttpStatus.OK);
    }

    @PostMapping("/front/course/unit/question/{questionId}/answer")
    public ResponseEntity<?> uploadAnswer(@PathVariable Long questionId, @Valid AnswerRequestDto answerRequestDto) {
        return new ResponseEntity<>(answerService.createAnswer(questionId, answerRequestDto), HttpStatus.OK);
    }

    @PatchMapping("/front/course/unit/question/answer/{answerId}")
    public ResponseEntity<?> updateAnswer(@RequestBody @Valid AnswerUpdateParam answerUpdateParam) {
        return new ResponseEntity<>(answerService.updateAnswer(answerUpdateParam), HttpStatus.OK);
    }

    @DeleteMapping("/front/course/unit/question/answer/{answerId}")
    public ResponseEntity<?> deleteAnswer(@PathVariable Long answerId) {
        answerService.deleteAnswer(answerId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/front/course/unit/question/answer/{answerId}")
    public ResponseEntity<?> getAnswerInfo(@PathVariable Long answerId) {
        return new ResponseEntity<>(answerService.getAnswerInfo(answerId), HttpStatus.OK);
    }
}
