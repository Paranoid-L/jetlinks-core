package org.jetlinks.core.command;

import org.jetlinks.core.metadata.FunctionMetadata;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractCommandSupport implements CommandSupport {

    protected final Map<Object, CommandHandler<Command<?>, ?>> handlers = new ConcurrentHashMap<>();

    @SuppressWarnings("all")
    protected final <C extends Command<R>, R> void registerHandler(CommandHandler<C, R> handler) {
        registerHandler(handler.createCommand().getCommandId(), handler);
    }

    @SuppressWarnings("unchecked")
    protected final <C extends Command<R>, R> void registerHandler(Class<C> type,
                                                                   CommandHandler<C, R> handler) {
        FunctionMetadata metadata = handler.getMetadata();

        handlers.put(type, (CommandHandler<Command<?>, ?>) handler);

        if (null != metadata) {
            registerHandler(metadata.getId(), handler);
        }
    }

    @SuppressWarnings("all")
    protected final <C extends Command<R>, R> void registerHandler(String id,
                                                                   CommandHandler<C, R> handler) {
        handlers.put(id, (CommandHandler<Command<?>, ?>) handler);
    }

    @Nonnull
    @Override
    public final <R> R execute(@Nonnull Command<R> command) {

        //直接执行可执行的指令
        if (command instanceof ExecutableCommand) {
            return ((ExecutableCommand<R>) command).execute(this);
        }

        //从注册的执行器中获取处理器进行执行
        CommandHandler<Command<?>, ?> handler = handlers.get(command.getCommandId());

        if (null != handler) {
            @SuppressWarnings("unchecked")
            R response = (R) handler.handle(command, this);
            return response;
        }

        return executeUndefinedCommand(command);
    }

    @Override
    public final <R, C extends Command<R>> C createCommand(String commandId) {
        CommandHandler<Command<?>, ?> handler = handlers.get(commandId);

        if (null != handler) {
            @SuppressWarnings("unchecked")
            C cmd = (C) handler.createCommand();
            return cmd;
        }
        return createUndefinedCommand(commandId);
    }

    @Override
    public final Flux<FunctionMetadata> getCommandMetadata() {
        return Flux
            .fromIterable(handlers.values())
            .distinct()
            .mapNotNull(CommandHandler::getMetadata);
    }

    @Override
    public final Mono<FunctionMetadata> getCommandMetadata(String commandId) {
        return Mono.justOrEmpty(getRegisteredMetadata(commandId));
    }

    public Optional<FunctionMetadata> getRegisteredMetadata(String commandId) {
        CommandHandler<Command<?>, ?> handler = handlers.get(commandId);
        if (handler != null) {
            return Optional.ofNullable(handler.getMetadata());
        }
        return Optional.empty();
    }

    protected <R, C extends Command<R>> C createUndefinedCommand(String commandId) {
        throw new CommandException(this, null, "error.unsupported_create_command", null, commandId);
    }

    @SuppressWarnings("all")
    protected <R> R executeUndefinedCommand(@Nonnull Command<R> command) {
        CommandException error = new CommandException(
            this,
            command,
            "error.unsupported_execute_command",
            null,
            CommandUtils.getCommandIdByType(command.getClass()));

        if (CommandUtils.commandResponseFlux(command)) {
            return (R) Flux.error(error);
        }
        if (CommandUtils.commandResponseMono(command)) {
            return (R) Mono.error(error);
        }
        throw error;
    }
}
