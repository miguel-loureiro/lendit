## **Item management system**

### Persistence: PostgresSQL


### Razões para o mapeamento direto de RegisterUserDto para utilizador:

- Preocupações de segurança: Como as palavras-passe são dados confidenciais, manipulá-las o menos possível é uma boa prática.
Ao mapear diretamente de RegisterUserDto para User, garante que a palavra-passe é processada apenas quando necessário – durante a criação da entidade User – e depois imediatamente codificada. 
A introdução de um UserDto no meio pode aumentar o risco de manuseamento incorreto ou de exposição inadvertida da palavra-passe.
- Responsabilidade única: O RegisterUserDto foi concebido especificamente para capturar a entrada necessária para o registo do utilizador, incluindo a palavra-passe. 
Isto alinha bem com a criação de uma entidade Utilizador, que requer a palavra-passe. O UserDto, por outro lado, destina-se a encapsular os dados do utilizador sem informações confidenciais, como a palavra-passe. 
Assim, envolver o UserDto no processo de registo poderia violar o princípio da responsabilidade única, obrigando-o a lidar com dados para os quais não foi concebido.
- Evitar complexidade desnecessária: o mapeamento direto de RegisterUserDto para User mantém o código simples e claro. 
A introdução de um passo extra em que o RegisterUserDto é primeiro convertido em UserDto e depois em User adicionaria complexidade desnecessária sem benefícios significativos. 
Também pode levar a possíveis problemas, como esquecer-se de codificar a palavra-passe corretamente durante a conversão.
- Desempenho: a conversão direta de RegisterUserDto em User evita um passo extra de transformação, que pode ser insignificante em termos de desempenho, mas ainda assim contribui para a eficiência global.


### Item request service

 Create a new request

``` itemRequestService.createRequest(userId, itemId); ```

 When an item is returned

``` itemRequestService.processReturnedItem(item); ```

If user doesn't create loan within notification period

```` itemRequestService.handleExpiredNotification(request); ````

// Cancel a request

```itemRequestService.cancelRequest(requestId); ```

LoanService

- Focus solely on loan management operations
- Delegates all request-related operations to ItemRequestService
- Only interacts with ItemRequestService through well-defined methods
- Each method has a single responsibility (creating, extending, or terminating loans)

ItemRequestService

- Handles all request-related operations
- Manages the request queue
- Processes notifications and status changes
- Handles request validation
- Provides clear public interfaces for other services to use

Key Changes:

- Moved all queue management logic to ItemRequestService
- Created clear boundaries between services
- Added new methods for better encapsulation:

findActiveRequestForUser: Encapsulates request lookup logic
handleItemReturn: Manages the return process
fulfillRequest: Updates request status when loan is created

- Split complex operations into smaller, focused private methods
- Improved validation by isolating it in dedicated methods

Communication Flow:

- LoanService -> ItemRequestService: For checking requests and handling returns

- ItemRequestService -> LoanService: No direct communication (follows better dependency direction)