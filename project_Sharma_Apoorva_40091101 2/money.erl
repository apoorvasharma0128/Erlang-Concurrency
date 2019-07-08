%%%-------------------------------------------------------------------
%%% @author apoorvasharma
%%% @copyright (C) 2019, <COMPANY>
%%% @doc
%%% This module is responsible for creating the bank and customer threads and
%%% displaying the result of various operations performed by customer anf bank.
%%% @end
%%% Created : 11. Jun 2019 22:42
%%%-------------------------------------------------------------------
-module(money).
-author("apoorvasharma").

%% API
-export([start/0,customer_process/3,bank_process/3,receive_msg/2,bank_final_status/2]).
-import(customer,[init_customer/5]).
-import(bank,[init_bank/5]).
-import(erlang,[send/2]).

start()->
  io:fwrite("** Customers and loan objectives ** ~n" ),
  CustomerFile=file:consult("customers.txt"),
  Clist =element(2,CustomerFile),
  CustomerMap =maps:from_list(Clist),
  maps:fold(fun(Key, Value, ok) -> io:format("~p: ~p~n", [Key, Value]) end, ok, CustomerMap),
  io:fwrite("~n"),
  io:fwrite("** Banks and financail resources ** ~n" ),
  BankFile=file:consult("banks.txt"),
  Blist =element(2,BankFile),
  BankMap = maps:from_list(Blist),
  maps:fold(fun(Key, Value, ok) -> io:format("~p: ~p~n", [Key, Value]) end, ok, BankMap),
  io:fwrite("~n"),
  Cust=maps:keys(CustomerMap),
  bank_process(BankMap,map_size(BankMap),Cust),
  customer_process(CustomerMap,map_size(CustomerMap),BankMap),
  Bank=maps:keys(BankMap),
  receive_msg(Cust,Bank).

receive_msg(Cust,Bank)->
  if
    length(Cust)==0 ->
      bank_final_status(Bank,length(Bank));
    true -> ok
  end,
  receive
    {Message}->
      io:fwrite("~s~n",[Message]),
      receive_msg(Cust,Bank);
    {Message,1,Key}->
      CustU=lists:delete(Key,Cust),
      io:fwrite("~s~n",[Message]),
      receive_msg(CustU,Bank);
    {1,Message,Bid}->
      io:fwrite("~s~n",[Message]),
      exit(Bid,"No Money Left"),
      receive_msg(Cust,Bank);
    {2,Message,Bid}->
      io:fwrite("~s~n",[Message]),
      exit(Bid,"No Money Left"),
      receive_msg(Cust,Bank)
  end,receive_msg(Cust,Bank).


customer_process(Map, 0,BM) ->
  done;
customer_process(Map, Times,BM) ->
  L=maps:keys(Map),
  Key = lists:nth(Times, L),
  Value = maps:get(Key,Map),
  Pid = spawn(customer,init_customer,[Key,Value,Value,maps:keys(BM),self()]),
  R=whereis(Key),
  if R/=undefined
     -> unregister(Key);
    true -> ok
  end,
  register(Key, Pid),
  send(Pid,{"Start"}),
  customer_process(Map, Times - 1,BM).

bank_process(Map,0,Cust) ->
  done;
bank_process(Map,Times,Cust) ->
  L=maps:keys(Map),
  Key = lists:nth(Times, L),
  Value = maps:get(Key,Map),
  Pid = spawn(bank, init_bank,[Key,Value,Value,Cust,self()]),
  R=whereis(Key),
  if R/=undefined
    -> unregister(Key);
    true -> ok
  end,
  register(Key, Pid),
  bank_process(Map, Times - 1,Cust).

bank_final_status(List,0) ->
  done;
bank_final_status(List,Times) ->
  L=lists:nth(Times,List),
  M=whereis(L),
  if
    M/=undefined -> send(M,{"get amount"});
    true -> ok
  end,
bank_final_status(List, Times - 1).