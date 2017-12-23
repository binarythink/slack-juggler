# slack-juggler

## What is Slack-Juggler?
KBS 드라마 `저글러스`의 의미를 빌려 비서를 비유적으로 표현  
> Jugglers!  
  양손과 양발로 수십 가지 일을 하면서도 보스의 가려운 부분을 긁어줄 줄 아는 저글링 능력자 언니들을 아는가?  
  어디선가, 보스에게, 무슨 일이 생기면 반드시 나타나는 오피스 히로인즈!  
  그 이름하야, 저글러스!
  
슬랙에서 비서 노릇하는 봇 입니다

## How to use it?
### Add slack bot in user workspace.
* https://slack.com/apps/A0F7YS25R-bots 를 사용하고자 하는 워크스페이스에 설치한다
* `src/main/resources`에 위치한 `application.yml.dist` 를 참고하여 `application.yml`를 만든다
* 환경설정을 하고 `api token` 정보를 `application.yml` 의 `slack.config.token` 정보에 넣는다 

## Features
추가 기능을 구현하고자 하는 경우 `JugglerService` 를 상속받아 `isTrigger()` 와 `execute()` 를 구현할 수 있습니다
* `JiraConnectService` : 대화내용에 이슈번호를 찾아 이슈의 기본적인 정보를 제공합니다
* `PlantUmlService` : `plantuml` 기능을 사용해 대화내용에 plantuml 구문을 인식하여 UML을 만들어 줍니다

## Thanks for ... (depend on)
 * SpringBoot [site](https://projects.spring.io/spring-boot/)
 * lombok [site](https://projectlombok.org/) [repository](https://github.com/rzwitserloot/lombok)
 * simple-slack-api [repository](https://github.com/Ullink/simple-slack-api)