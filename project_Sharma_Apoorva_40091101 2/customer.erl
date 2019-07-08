%%%-------------------------------------------------------------------
%%% @author apoorvasharma
%%% @copyright (C) 2019, <COMPANY>
%%% @doc
%%% This module contains all the functionality related to customer operations.
%%% The main task is to request loan from bank.
%%% It maintains a bank list which is initialized at thread creation time which contains all available
%%% banks. If the list becomes empty or objective is reached,it displays the appropriate message and exists.
%%% @end
%%% Created : 11. Jun 2019 23:08
%%%-------------------------------------------------------------------
-module(customer).
-author("apoorvasharma").

%% API
-import(rand,[uniform/1]).
-import(erlang,[send/2]).
-export([init_customer/5]).

init_customer(Name,IValue,Mvalue,PotentialBank,Parent)->
  receive
    {"Approved",Bname,Amount,Bid}->
      MV= Mvalue-Amount,
      Message =io_lib:format("~s",[Bname])++" approved a loan of "++io_lib:format("~w",[Amount])++" dollar(s) from "++io_lib:format("~w",[Name]),
      send(Parent,{Message}),
      apply_loan(Name,IValue,MV,PotentialBank,Parent),
      init_customer(Name,IValue,MV,PotentialBank,Parent);

    {"Denied",Bname,Amount}->
      PB=lists:delete(Bname, PotentialBank),
      Message =io_lib:format("~s",[Bname])++" denied a loan of "++io_lib:format("~w",[Amount])++" dollar(s) from "++io_lib:format("~w",[Name]),
      send(Parent,{Message}),
      apply_loan(Name,IValue,Mvalue,PB,Parent),
      init_customer(Name,IValue,Mvalue,PB,Parent);

    {"Start"}->
      apply_loan(Name,IValue,Mvalue,PotentialBank,Parent)
%%      init_customer(Name,IValue,Mvalue,PotentialBank,Parent)
  end,
  init_customer(Name,IValue,Mvalue,PotentialBank,Parent).

apply_loan(Name,IValue,MValue,PotentialBank,Parent)->

  if MValue==0
    ->
    Msg=io_lib:format("~s",[Name])++" has reached the objective of "++io_lib:format("~w",[IValue])++" dollar(s).",
    send(Parent,{Msg,1,Name}),
    timer:sleep(50),
    exit(self(), "Completed");
    true ->
      if
        length(PotentialBank) ==0
          ->
          BValue= IValue - MValue,
          Msg1=io_lib:format("~s",[Name])++" was only able to borrow "++io_lib:format("~w",[BValue])++" dollar(s).",
          send(Parent,{Msg1,1,Name}),
          timer:sleep(50),
          exit(self(), "Completed");
        true ->
          L=length(PotentialBank),
          RandomBank = uniform(L),
          BankName = lists:nth(RandomBank, PotentialBank),
          if
            MValue>50 -> RandomAmount = uniform(50) ;
            true ->
              RandomAmount = uniform(MValue)
          end,
          Message =io_lib:format("~s",[Name])++" requests a loan of "++io_lib:format("~w",[RandomAmount])++" dollar(s) from "++io_lib:format("~w",[BankName]),
          RT=uniform(100),
          if
            RT<10 -> RTS=10+RT;
            true -> RTS=RT
          end,
          timer:sleep(RTS),
          send(Parent,{Message}),
          timer:sleep(RTS),
          send(BankName,{"Apply",Name,RandomAmount,self()})
      end
  end.




