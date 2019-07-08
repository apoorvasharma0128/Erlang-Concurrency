%%%-------------------------------------------------------------------
%%% @author apoorvasharma
%%% @copyright (C) 2019, <COMPANY>
%%% @doc
%%% This module contains all the functionality related to bank operations.
%%% The main task is to approve or deny the loan request requested by the customer.
%%% It maintains a customer list which is initialized at thread creation time which conntains all available
%%% customers. If the list becomes empty,or amount remaining with bank becomes 0 or process is finished,
%%% the remaining amount of bank is displayed and the thread is exited.
%%% @end
%%% Created : 13. Jun 2019 22:51
%%%-------------------------------------------------------------------
-module(bank).
-author("apoorvasharma").

%% API
-export([init_bank/5]).
-import(erlang,[send/2]).
-import(rand,[uniform/1]).
init_bank(Bname,IValue,MValue,Cust,Parent)->
  RT=uniform(100),
  if
    RT<10 -> RTS=10+RT;
    true -> RTS=RT
  end,
  timer:sleep(RTS),
  receive
    {"Apply",Borrower,Amount,Bid}->
      if Amount=<MValue->
        MV=MValue-Amount,
        send(Bid,{"Approved",Bname,Amount,self()}),
        init_bank(Bname,IValue,MV,Cust,Parent);
        true ->
          MV=MValue,
          CustUpdated =lists:delete(Borrower,Cust),
          send(Bid,{"Denied",Bname,Amount}),
          if
            length(CustUpdated)==0 ->
              Msg1 =io_lib:format("~s",[Bname])++" has "++io_lib:format("~w",[MValue])++" dollar(s) remaining.",
              timer:sleep(50),
              send(Parent,{1,Msg1,self()}),
              timer:sleep(50),
              exit(self(),"No more operations can be performed!");
            true -> ok
          end,
init_bank(Bname,IValue,MValue,CustUpdated,Parent)
      end;
    {"get amount"}->
      Msg =io_lib:format("~s",[Bname])++" has "++io_lib:format("~w",[MValue])++" dollar(s) remaining.",
      send(Parent,{2,Msg,self()}),
      timer:sleep(50),
      init_bank(Bname,IValue,MValue,Cust,Parent)
end,
  init_bank(Bname,IValue,MValue,Cust,Parent).